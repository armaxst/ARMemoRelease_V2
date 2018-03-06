//
//  LearnViewController.swift
//  ARMemoApp
//
//  Created by 이상훈 on 2018. 2. 14..
//  Copyright © 2018년 Ray. All rights reserved.
//

import UIKit
import AVFoundation
import ARMemoFramework

class LearnViewController: UIViewController, AVCaptureVideoDataOutputSampleBufferDelegate, CaptureDelegate {
    
    @IBOutlet weak var previewView: UIView!
    @IBOutlet weak var captureView: UIView!
    @IBOutlet weak var drawingView: UIImageView!
    @IBOutlet weak var trackingButton: UIButton!
    @IBOutlet weak var captureButton: UIButton!
    @IBOutlet weak var clearButton: UIButton!
    @IBOutlet weak var backButton: UIButton!
    @IBOutlet weak var resolutionLabel: UILabel!
    
    var captureSession : AVCaptureSession?
    var videoPreviewLayer : AVCaptureVideoPreviewLayer?
    var CaptureVC = CaptureViewController()
    var isInitTracker : Bool = false
    var isStartTracker : Bool = false
    var isCapturing : Bool = false
    var imageByteData : UnsafeMutablePointer<UInt8>! = nil
    var imageLength : Int32 = -1
    var imageWidth : Int32 = -1
    var imageHeight : Int32 = -1
    var drawPointArray : NSArray? = nil
    
    var screenWidth : Int32 = 0
    var screenHeight : Int32 = 0
    
    //MARK: - view Override
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        self.navigationController?.navigationBar.isHidden = true
        ARMemo.initialize("w8EoHToosk/K/2dhysr8Zg/5IBlBjwe8YXgoUDJMfmY=")
        
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
        
        //start video capture
        captureSession?.startRunning()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        captureSession?.stopRunning()
        ARMemo.stopTracking()
        ARMemo.destory()
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
    
    //MARK: - Button Action
    @IBAction func clickBackButton(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func changeTrackingState(_ sender: Any) {
        isInitTracker = !isInitTracker

        if isInitTracker == true
        {
            trackingButton.setTitle("Stop", for: UIControlState.normal)
            captureButton.isHidden = false
           
            ARMemo.startTracking()
        }
        else
        {
            isCapturing = false
            if isStartTracker == true
            {
                ARMemo.clearTrackingTrackable()
                if drawPointArray?.count != 0
                {
                    drawPointArray = nil
                    drawingView.isHidden = true
                }
                isStartTracker = false
            }
            
            trackingButton.setTitle("Start", for: UIControlState.normal)
            captureButton.isHidden = true
            clearButton.isHidden = true
            
            ARMemo.stopTracking()
        }
    }
    @IBAction func clickCaptureButton(_ sender: Any) {
        if isInitTracker == true
        {
            if isStartTracker == true
            {
                ARMemo.clearTrackingTrackable()
                if drawPointArray?.count != 0
                {
                    drawPointArray = nil
                    drawingView.isHidden = true
                }
                isStartTracker = false
            }
            isCapturing = true
            clearButton.isHidden = true
        }
    }
    
    @IBAction func clickClearButton(_ sender: Any) {
        if isInitTracker == true
        {
            if isStartTracker == true
            {
                ARMemo.clearTrackingTrackable()
                if drawPointArray?.count != 0
                {
                    drawPointArray = nil
                    drawingView.isHidden = true
                }
                isStartTracker = false
            }
            clearButton.isHidden = true
        }
    }
    
    //MARK: - Private Function
    private func initUIObject() {
        backButton.layer.borderWidth = 1.0
        backButton.layer.cornerRadius = CGFloat(roundf(Float(backButton.frame.size.width / 2.0)))
        backButton.layer.masksToBounds = true
        
        drawingView.backgroundColor = UIColor.clear
        drawingView.isHidden = true
        
        captureView.isHidden = true
        
        captureButton.isHidden = true
        clearButton.isHidden = true
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
        
        if isInitTracker == true
        {
            if isCapturing == true
            {
                let result : Int32 = ARMemo.checkLearnable(byteBuffer, length: Int32(tiLen), width: Int32(bufferWidth), height: Int32(bufferHeight), format: 2)
                if result == 0
                {
                    DispatchQueue.main.async {
                        self.CaptureVC.setCaptureImage(imageBuffer: pixelBuffer)
                        self.captureView.isHidden = false
                    }
                    
                    if(imageByteData != nil)
                    {
                        imageByteData = nil
                        imageLength = -1
                        imageWidth = -1
                        imageHeight = 1
                    }
                    
                    imageByteData = UnsafeMutablePointer<UInt8>.allocate(capacity: tiLen)
                    memcpy(imageByteData, byteBuffer, tiLen)
                    imageLength = Int32(tiLen)
                    imageWidth = Int32(bufferWidth)
                    imageHeight = Int32(bufferHeight)
                    isCapturing = false
                }
            }
            
            if isStartTracker == true
            {
                ARMemo.inputTrackingImage(byteBuffer, length: Int32(tiLen), width: Int32(bufferWidth), height: Int32(bufferHeight), format: 2)

                let transformMatrix : UnsafeMutablePointer<Float>? = UnsafeMutablePointer.allocate(capacity: 9)
                let result : Int32 = ARMemo.getTrackingResult(transformMatrix)
                
                let resizingSize = Utilites.getResizeView(cameraWidth: CGFloat(imageWidth), cameraHeight: CGFloat(imageHeight), screenWidth: CGFloat(screenWidth), screenHeight: CGFloat(screenHeight))
                
                DispatchQueue.main.async {
                    
                    if self.drawingView.frame.size.width == self.view.frame.size.width && self.drawingView.frame.size.height == self.view.frame.size.height
                    {
                        self.drawingView.frame = CGRect(x: 0, y: 0, width: resizingSize.0, height: resizingSize.1)
                        self.drawingView.center = CGPoint(x: self.view.frame.size.width / 2, y: self.view.frame.size.height / 2)
                    }
                }
                
                if result == 0
                {
                    if drawPointArray == nil
                    {
                        return
                    }
                    let arrCount : Int = (drawPointArray?.count)!

                    let totalPath : UIBezierPath = UIBezierPath()
                    for i in  0..<arrCount
                    {
                        let tempArr : NSArray = drawPointArray![i] as! NSArray
                        let tempArrCount : Int = (tempArr.count)

                        let path : UIBezierPath = UIBezierPath()
                        for j in 0..<tempArrCount
                        {
                            let tempDic : NSDictionary = tempArr[j] as! NSDictionary
                            let tempPoint : CGPoint = CGPoint.init(x: tempDic.object(forKey:"x") as! CGFloat, y: tempDic.object(forKey:"y") as! CGFloat)

                            let calcZ : Float = (transformMatrix![6] * Float(tempPoint.x)) + (transformMatrix![7] * Float(tempPoint.y)) + transformMatrix![8]

                            if calcZ != 0
                            {
                                let calcX : Float = ((transformMatrix![0] * Float(tempPoint.x)) + (transformMatrix![1] * Float(tempPoint.y)) + transformMatrix![2]) / calcZ
                                let calcY : Float = ((transformMatrix![3] * Float(tempPoint.x)) + (transformMatrix![4] * Float(tempPoint.y)) + transformMatrix![5]) / calcZ
                                
                                let screenPoint : CGPoint = Utilites.imageToScreen(screenWidth: Int32(resizingSize.0), screenHeight: Int32(resizingSize.1), imageWidth: imageWidth, imageHeight: imageHeight, imageX: CGFloat(calcX), imageY: CGFloat(calcY))
                                
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
                
                transformMatrix?.deallocate(capacity: 9)
            }
        }

        CVPixelBufferUnlockBaseAddress(pixelBuffer, CVPixelBufferLockFlags(rawValue: 0))
    }
    
    //MARK: - Capture Delegate
    func confirmDraw(pointArray: NSArray, size: Int32) {
        ARMemo.learn(imageByteData, length: imageLength, width: imageWidth, height: imageHeight, format: 2, strokeInfo: pointArray as! [Any], size: size)
        
        let trackableFilePath = DataFileManager.getTrackableDataPath()
        let result = ARMemo.saveLearnedFile(trackableFilePath)
        if result != 0
        {
            print("Trackable Data File save Failed")
        }
        else
        {
            print("Trackable Data File save Success")
            let state :Bool = DataFileManager.saveStrokeData(imageWidth: imageWidth, imageHeight: imageHeight, object: pointArray)
            if state == false
            {
                print("Stroke Data File save Failed")
            }
            else
            {
                print("Stroke Data File save Success")
//                 self.navigationController?.popViewController(animated: true)
                ARMemo.clearLearnedTrackable()
                ARMemo.setTrackingFile(trackableFilePath)

                drawingView.isHidden = false
                if drawPointArray == nil
                {
                    drawPointArray = NSArray()
                }
                drawPointArray = pointArray.copy() as? NSArray

                captureView.isHidden = true
                clearButton.isHidden = false
                isStartTracker = true
            }
        }
    }
    
    func cancelDraw() {
        captureView.isHidden = true
    }
    
    //MARK: - Prepare
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if(segue.identifier == "CaptureView")
        {
            CaptureVC = segue.destination as! CaptureViewController
            CaptureVC.delegate = self
        }
    }
}

//MARK: - UIInterfaceOrientation
extension UIInterfaceOrientation {
    var videoOrientation: AVCaptureVideoOrientation? {
        switch self {
        case .portraitUpsideDown: return .portraitUpsideDown
        case .landscapeRight: return .landscapeRight
        case .landscapeLeft: return .landscapeLeft
        case .portrait: return .portrait
        default: return nil
        }
    }
}

