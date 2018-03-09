//
//  TrackingViewController.swift
//  ARMemoApp
//
//  Created by 이상훈 on 2018. 2. 22..
//  Copyright © 2018년 Ray. All rights reserved.
//

import UIKit
import AVFoundation
import ARMemoFramework

class TrackingViewController: UIViewController, AVCaptureVideoDataOutputSampleBufferDelegate {

    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var drawingView: UIImageView!
    
    @IBOutlet weak var resolutionLabel: UILabel!
    @IBOutlet weak var backButton: UIButton!
    
    var captureSession : AVCaptureSession?
    var videoPreviewLayer : AVCaptureVideoPreviewLayer?
    var strokeArray : NSArray? = nil
    var strokeInfo : [String : Any] = NSMutableDictionary() as! [String : Any]
    
    var screenWidth : Int32 = 0
    var screenHeight : Int32 = 0
    
    var imageWidth : Int32 = 0
    var imageHeight : Int32 = 0

    //MARK: - view Override
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        self.navigationController?.navigationBar.isHidden = true
        
        let state : Int32 = ARMemo.initialize("w8EoHToosk/K/2dhysr8Zg/5IBlBjwe8YXgoUDJMfmY=")
        if state != 0
        {
            let alert = UIAlertController(title: "Error", message: "App Signature is not Correct.", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler:
                { (action: UIAlertAction) in
                    exit(0)
            }))
            self.present(alert, animated: true, completion: nil)
        }
        
        screenWidth = Int32(self.view.frame.size.width)
        screenHeight = Int32(self.view.frame.size.height)
        
        initUIObject()
        
        captureSession = AVCaptureSession()
        
        captureSession?.beginConfiguration()
        
        addVideoInput()
        addVideoOutput()
        
        let width = Int32(UserDefaults.standard.string(forKey: "LearnWidth")!)
        let height = Int32(UserDefaults.standard.string(forKey: "LearnHeight")!)
        captureSession?.sessionPreset = findSimilarSize(width: width!, height: height!)
        captureSession?.commitConfiguration()
        
        //Initialise the video preview layer and add it as a sublayer to the viewPreview view's layer
        videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
        videoPreviewLayer?.videoGravity = AVLayerVideoGravity.resizeAspectFill
        videoPreviewLayer?.frame = view.layer.bounds
        previewView.layer.addSublayer(videoPreviewLayer!)
        
        ARMemo.start()
        let trackableFilePath = DataFileManager.getTrackableDataPath()
        ARMemo.setTrackingFile(trackableFilePath)
        
        strokeInfo = DataFileManager.loadStrokeData() as! [String : Any]
        
        imageWidth = strokeInfo["imageWidth"] as! Int32
        imageHeight = strokeInfo["imageHeight"] as! Int32
        strokeArray = strokeInfo["strokes"] as? NSArray
        
        //start video capture
        captureSession?.startRunning()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
    }
    
    override func viewDidLayoutSubviews() {
        videoPreviewLayer?.frame = view.bounds
        if let previewLayer = videoPreviewLayer ,(previewLayer.connection?.isVideoOrientationSupported)! {
            previewLayer.connection?.videoOrientation = UIApplication.shared.statusBarOrientation.videoOrientation ?? .landscapeLeft
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //MARK: - Private Function
    private func initUIObject() {
        backButton.layer.borderWidth = 1.0
        backButton.layer.cornerRadius = CGFloat(roundf(Float(backButton.frame.size.width / 2.0)))
        backButton.layer.masksToBounds = true
        
        drawingView.backgroundColor = UIColor.clear
    }
    
    private func findSimilarSize(width : Int32, height : Int32) -> AVCaptureSession.Preset {
        
        resolutionLabel.text = String(width) + "X" + String(height)
        if width == 640 && height == 480
        {
            return AVCaptureSession.Preset.vga640x480
        }
        else if width == 1280 && height == 720
        {
            return AVCaptureSession.Preset.hd1280x720
        }
        else if width == 1920 && height == 1080
        {
            return AVCaptureSession.Preset.hd1920x1080
        }
        else
        {
            return AVCaptureSession.Preset.vga640x480
        }
    }
    
    private func addVideoInput() {
        guard let captureDevice = AVCaptureDevice.default(for: AVMediaType.video) else {
            fatalError("No vidoe device found")
        }
        
        do {
            try captureDevice.lockForConfiguration()
        } catch {
            fatalError("Vidoe device lock failed")
        }
        
        for format in captureDevice.formats
        {
            let description : CMFormatDescription = format.formatDescription
            let maxrate : Float64 = format.videoSupportedFrameRateRanges[0].maxFrameRate
            
            if maxrate > 59 && CMFormatDescriptionGetMediaSubType(description) == kCVPixelFormatType_420YpCbCr8PlanarFullRange
            {
                captureDevice.activeFormat = format
                captureDevice.activeVideoMinFrameDuration = CMTimeMake(10, 600)
                captureDevice.activeVideoMaxFrameDuration = CMTimeMake(10, 600)
            }
        }
        
        if captureDevice.isFocusModeSupported(AVCaptureDevice.FocusMode.continuousAutoFocus)
        {
            print("AVCaptureFocusModeContinuousAutoFocus support")
            captureDevice.focusMode = AVCaptureDevice.FocusMode.continuousAutoFocus
        }
        else if captureDevice.isFocusModeSupported(AVCaptureDevice.FocusMode.autoFocus)
        {
            print("AVCaptureFocusModeAutoFocus support")
            captureDevice.focusMode = AVCaptureDevice.FocusMode.autoFocus
        }
        else
        {
            print("AVCaptureFocusModeContinuousAutoFocus And AVCaptureFocusModeAutoFocus not support")
        }
        
        captureDevice.unlockForConfiguration()
        
        do {
            let tCaptureDeviceInput = try AVCaptureDeviceInput(device: captureDevice) as AVCaptureDeviceInput
            
            if captureSession?.canAddInput(tCaptureDeviceInput) != nil
            {
                print("Capture Session addInput Success")
                captureSession?.addInput(tCaptureDeviceInput)
            }
        }
        catch {
            fatalError("AVCaptureDeviceInput Error")
        }
    }
    
    private func addVideoOutput() {
        let avCaptureVideoDataOutput = AVCaptureVideoDataOutput()
        
        let tDispatchQueue = DispatchQueue(label: "CameraImgQueue")
        avCaptureVideoDataOutput.alwaysDiscardsLateVideoFrames = true
        avCaptureVideoDataOutput.videoSettings = [(kCVPixelBufferPixelFormatTypeKey as NSString) as String: kCVPixelFormatType_420YpCbCr8BiPlanarFullRange]
        avCaptureVideoDataOutput.setSampleBufferDelegate(self, queue: tDispatchQueue)
        
        if captureSession?.canAddOutput(avCaptureVideoDataOutput) != nil
        {
            print("Capture Session addOutput Success")
            captureSession?.addOutput(avCaptureVideoDataOutput)
        }
    }
    
    //MARK: - Capture Output Delegate
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        
        let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)!
        
        CVPixelBufferLockBaseAddress(pixelBuffer, CVPixelBufferLockFlags(rawValue: 0))
        
        let bufferHeight : Int = CVPixelBufferGetHeight(pixelBuffer)
        let bufferWidth : Int = CVPixelBufferGetWidth(pixelBuffer)
        
        let tiLen : Int = CVPixelBufferGetDataSize(pixelBuffer)
        
        let baseAddress = CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0)
        let byteBuffer = unsafeBitCast(baseAddress, to:UnsafeMutablePointer<UInt8>.self)
        
        ARMemo.inputTrackingImage(byteBuffer, length: Int32(tiLen), width: Int32(bufferWidth), height: Int32(bufferHeight), format: 2)
        
        let transformMatrix : UnsafeMutablePointer<Float>? = UnsafeMutablePointer.allocate(capacity: 9)
        let result : Int32 = ARMemo.getTrackingResult(transformMatrix)
        
        let resizingSize = Utilites.getResizeView(cameraWidth: CGFloat(bufferWidth), cameraHeight: CGFloat(bufferHeight), screenWidth: CGFloat(screenWidth), screenHeight: CGFloat(screenHeight))
        
        DispatchQueue.main.async {
            if self.drawingView.frame.size.width == self.view.frame.size.width && self.drawingView.frame.size.height == self.view.frame.size.height
            {
                print("DrawingView Init Success")
                self.drawingView.frame = CGRect(x: 0, y: 0, width: resizingSize.0, height: resizingSize.1)
                self.drawingView.center = CGPoint(x: self.view.frame.size.width / 2, y: self.view.frame.size.height / 2)
            }
        }
        
        if result == 0
        {
            if strokeArray == nil
            {
                return
            }
            let arrCount : Int = (strokeArray?.count)!
            let totalPath : UIBezierPath = UIBezierPath()
            for i in  0..<arrCount
            {
                let tempArr : NSArray = strokeArray![i] as! NSArray
                let tempArrCount : Int = (tempArr.count)

                let path : UIBezierPath = UIBezierPath()
                for j in 0..<tempArrCount
                {
                    let tempDic : NSDictionary = tempArr[j] as! NSDictionary
                    
                    var pointX : CGFloat = tempDic.object(forKey:"x") as! CGFloat
                    var pointY : CGFloat = tempDic.object(forKey:"y") as! CGFloat

                    if bufferWidth != imageWidth && bufferHeight != imageHeight
                    {
                        let wr : CGFloat = CGFloat(bufferWidth) / CGFloat(imageWidth)
                        let hr : CGFloat = CGFloat(bufferHeight) / CGFloat(imageHeight)
                        let halfDiffHeight : CGFloat = (CGFloat(imageHeight) * wr - CGFloat(bufferHeight)) / 2.0
                        
                        pointX = pointX * wr
                        pointY = pointY * wr - halfDiffHeight
                    }
                    
                    let tempPoint : CGPoint = CGPoint.init(x: pointX, y: pointY)

                    let calcZ : Float = (transformMatrix![6] * Float(tempPoint.x)) + (transformMatrix![7] * Float(tempPoint.y)) + transformMatrix![8]

                    if calcZ != 0
                    {
                        let calcX : Float = ((transformMatrix![0] * Float(tempPoint.x)) + (transformMatrix![1] * Float(tempPoint.y)) + transformMatrix![2]) / calcZ
                        let calcY : Float = ((transformMatrix![3] * Float(tempPoint.x)) + (transformMatrix![4] * Float(tempPoint.y)) + transformMatrix![5]) / calcZ
                        
                        let screenPoint : CGPoint = Utilites.imageToScreen(screenWidth: Int32(resizingSize.0), screenHeight: Int32(resizingSize.1), imageWidth: Int32(bufferWidth), imageHeight: Int32(bufferHeight), imageX: CGFloat(calcX), imageY: CGFloat(calcY))
                        
                        if j == 0
                        {
                            path.move(to: screenPoint)
                        }
                        path.addLine(to: screenPoint)
                    }
                }

                totalPath.append(path)
            }

            DispatchQueue.main.async {
                self.drawingView.layer.sublayers?.forEach { $0.removeFromSuperlayer() }

                let shapeLayer = CAShapeLayer()

                shapeLayer.path = totalPath.cgPath
                shapeLayer.lineCap = "round"
                shapeLayer.lineJoin = "round"
                shapeLayer.strokeColor = UIColor.red.cgColor
                shapeLayer.fillColor = UIColor.clear.cgColor
                shapeLayer.lineWidth = 3.0

                self.drawingView.layer.addSublayer(shapeLayer)
            }
        }
        else
        {
            DispatchQueue.main.async {
                self.drawingView.layer.sublayers?.forEach { $0.removeFromSuperlayer() }
            }
        }
        
        CVPixelBufferUnlockBaseAddress(pixelBuffer, CVPixelBufferLockFlags(rawValue: 0))
    }
    
    //MARK: - Button Action
    @IBAction func clickBackButton(_ sender: Any) {
        strokeArray = nil
        captureSession?.stopRunning()
        ARMemo.clearTrackingTrackable()
        
        ARMemo.stop()
        ARMemo.destory()
        
        self.navigationController?.popViewController(animated: true)
    }
}


