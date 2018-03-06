//
//  CaptureViewController.swift
//  ARMemoApp
//
//  Created by 이상훈 on 2018. 2. 14..
//  Copyright © 2018년 Ray. All rights reserved.
//

import UIKit
import ARMemoFramework

protocol CaptureDelegate {
    func confirmDraw(pointArray : NSArray, size : Int32)
    func cancelDraw()
}

class CaptureViewController: UIViewController {

    var delegate: CaptureDelegate?
    var lastPoint : CGPoint = CGPoint.zero
    var pointArray : NSMutableArray?
    var totalPointArray : NSMutableArray = NSMutableArray()
    var isSwiped : Bool = false
    
    var imageWidth : Int32 = 0
    var imageHeight : Int32 = 0

    @IBOutlet weak var captureImage: UIImageView!
    @IBOutlet weak var drawingImage: UIImageView!
    @IBOutlet weak var okButton: UIButton!
    @IBOutlet weak var cancelButton: UIButton!
    
    //MARK: - view Override
    override func viewDidLoad() {
        super.viewDidLoad()

        okButton.layer.borderWidth = 1.0
        okButton.layer.borderColor = UIColor.black.cgColor
        okButton.layer.cornerRadius = 5.0
        okButton.layer.masksToBounds = true
        okButton.setBackgroundImage(Utilites.imageWithColor(color: UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0)), for: UIControlState.highlighted)
        

        cancelButton.layer.borderWidth = 1.0
        cancelButton.layer.borderColor = UIColor.black.cgColor
        cancelButton.layer.cornerRadius = 5.0
        cancelButton.layer.masksToBounds = true
        cancelButton.setBackgroundImage(Utilites.imageWithColor(color: UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0)), for: UIControlState.highlighted)
        
        drawingImage.isHidden = true
        // Do any additional setup after loading the view.
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        totalPointArray.removeAllObjects()
        isSwiped = false
        
        imageWidth = 0
        imageHeight = 0
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    public func setCaptureImage( imageBuffer : CVImageBuffer!)
    {
        totalPointArray.removeAllObjects()
        if captureImage.image != nil
        {
            captureImage.image = nil
        }
        
        imageWidth = Int32(CVPixelBufferGetHeight(imageBuffer))
        imageHeight = Int32(CVPixelBufferGetWidth(imageBuffer))
        
        var coreImage : CIImage = CIImage.init(cvImageBuffer: imageBuffer)
        coreImage = coreImage.oriented(forExifOrientation: 3)

        let tempContext : CIContext = CIContext.init(options: nil)
        let cgImage : CGImage = tempContext.createCGImage(coreImage, from: coreImage.extent)!
        
        let image : UIImage = UIImage.init(cgImage: cgImage, scale: UIScreen.main.scale, orientation: UIImageOrientation.up)

        captureImage.contentMode = UIViewContentMode.scaleAspectFill
        captureImage.image = image

        let resizingSize = Utilites.getResizeView(cameraWidth: CGFloat(imageHeight), cameraHeight: CGFloat(imageWidth), screenWidth: self.view.frame.size.width, screenHeight: self.view.frame.size.height)

        drawingImage.frame = CGRect(x: 0, y: 0, width: resizingSize.0, height: resizingSize.1)
        drawingImage.center = CGPoint(x: self.view.frame.size.width / 2, y: self.view.frame.size.height / 2)
        drawingImage.isHidden = false
    }
        
    //MARK: - Button Action
    @IBAction func clickOKButton(_ sender: Any) {
        if totalPointArray.count == 0
        {
            let alert = UIAlertController(title: "Error", message: "At least one Point is needed.", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil))
            self.present(alert, animated: true, completion: nil)
            return
        }
        
        drawingImage.image = nil
        drawingImage.isHidden = true
        delegate?.confirmDraw(pointArray: totalPointArray, size: Int32(totalPointArray.count))
    }
    
    @IBAction func clickCancelButton(_ sender: Any) {
        totalPointArray.removeAllObjects()
        drawingImage.image = nil
        drawingImage.isHidden = true
        delegate?.cancelDraw()
    }
    
    //MARK: - touch Event Override
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        if captureImage.image == nil
        {
            return
        }
        
        if let touch = touches.first {
            let currentPoint = touch.location(in: drawingImage)

            let imagePoint : CGPoint = Utilites.screenToImage(screenWidth: Int32(drawingImage.frame.size.width), screenHeight: Int32(drawingImage.frame.size.height), imageWidth: imageWidth, imageHeight: imageHeight, touchX: currentPoint.x, touchY: currentPoint.y)
            
            let dicTemp = ["x" : imagePoint.x, "y" : imagePoint.y]
            
            pointArray = NSMutableArray()
            pointArray?.add(dicTemp)
            lastPoint = currentPoint
            isSwiped = false
        }
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        if captureImage.image == nil
        {
            return
        }
        
        if let touch = touches.first {

            let currentPoint = touch.location(in: drawingImage)
            
            isSwiped = true
           
            UIGraphicsBeginImageContextWithOptions(self.drawingImage.frame.size, false, 0)
            drawingImage.image?.draw(in: CGRect(x: 0, y: 0, width: self.drawingImage.frame.size.width, height: self.drawingImage.frame.size.height))

            let ctx : CGContext = UIGraphicsGetCurrentContext()!

            ctx.setStrokeColor(UIColor.red.cgColor)
            ctx.setLineWidth(3.0)

            ctx.setLineJoin(CGLineJoin.round)
            ctx.setLineCap(CGLineCap.round)

            ctx.beginPath()
            ctx.move(to: lastPoint)
            ctx.addLine(to: currentPoint)
            ctx.strokePath()

            drawingImage.image = UIGraphicsGetImageFromCurrentImageContext()
            
            UIGraphicsEndImageContext()
            
            let imagePoint : CGPoint = Utilites.screenToImage(screenWidth: Int32(drawingImage.frame.size.width), screenHeight: Int32(drawingImage.frame.size.height), imageWidth: imageWidth, imageHeight: imageHeight, touchX: currentPoint.x, touchY: currentPoint.y)
            let dicTemp = ["x" : imagePoint.x, "y" : imagePoint.y]
            pointArray?.add(dicTemp)
            lastPoint = currentPoint
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        if captureImage.image == nil
        {
            return
        }
        
        if let touch = touches.first {
            let currentPoint = touch.location(in: drawingImage)
            
            if isSwiped == false
            {
                UIGraphicsBeginImageContextWithOptions(self.drawingImage.frame.size, false, 0)
                drawingImage.image?.draw(in: CGRect(x: 0, y: 0, width: self.drawingImage.frame.size.width, height: self.drawingImage.frame.size.height))
                
                let ctx : CGContext = UIGraphicsGetCurrentContext()!
                
                ctx.setStrokeColor(UIColor.red.cgColor)
                ctx.setLineWidth(3.0)
                
                ctx.setLineJoin(CGLineJoin.round)
                ctx.setLineCap(CGLineCap.round)
                
                ctx.beginPath()
                ctx.move(to: lastPoint)
                ctx.addLine(to: currentPoint)
                ctx.strokePath()
                
                drawingImage.image = UIGraphicsGetImageFromCurrentImageContext()
                
                UIGraphicsEndImageContext()
            }
            
            let imagePoint : CGPoint = Utilites.screenToImage(screenWidth: Int32(drawingImage.frame.size.width), screenHeight: Int32(drawingImage.frame.size.height), imageWidth: imageWidth, imageHeight: imageHeight, touchX: currentPoint.x, touchY: currentPoint.y)
            let dicTemp = ["x" : imagePoint.x, "y" : imagePoint.y]
            pointArray?.add(dicTemp)
            totalPointArray.add(pointArray as Any)
        }
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
