//
//  Utilities.swift
//  ARMemoApp
//
//  Created by 이상훈 on 2018. 2. 14..
//  Copyright © 2018년 Ray. All rights reserved.
//

import UIKit
import simd

class Utilites {
    class func screenToImage (screenWidth : Int32, screenHeight : Int32, imageWidth : Int32, imageHeight : Int32, touchX : CGFloat, touchY : CGFloat) -> CGPoint
    {
        let scaleX : CGFloat = touchX / CGFloat(screenWidth)
        let scaleY : CGFloat = touchY / CGFloat(screenHeight)

        let resultX : CGFloat = CGFloat(imageHeight) * scaleX
        let resultY : CGFloat = CGFloat(imageWidth) * scaleY
        
        return CGPoint(x: resultX, y: resultY)
    }

    class func imageToScreen (screenWidth : Int32, screenHeight : Int32, imageWidth : Int32, imageHeight : Int32, imageX : CGFloat, imageY : CGFloat) -> CGPoint
    {
        let scaleX : CGFloat = imageX / CGFloat(imageWidth)
        let scaleY : CGFloat = imageY / CGFloat(imageHeight)
        
        let resultX : CGFloat = CGFloat(screenWidth) * scaleX
        let resultY : CGFloat = CGFloat(screenHeight) * scaleY
        
        return CGPoint(x: resultX, y: resultY)
    }
 
    class func makeMatrix (data : UnsafeMutablePointer<Float>) -> matrix_float3x3
    {
        var temp : matrix_float3x3 = matrix_identity_float3x3
        
        temp.columns.0.x = data[0]
        temp.columns.0.y = data[1]
        temp.columns.0.z = data[2]
        
        temp.columns.1.x = data[3]
        temp.columns.1.y = data[4]
        temp.columns.1.z = data[5]
        
        temp.columns.2.x = data[6]
        temp.columns.2.y = data[7]
        temp.columns.2.z = data[8]

        return temp
    }
    
    class func getResizeView (cameraWidth : CGFloat, cameraHeight : CGFloat, screenWidth : CGFloat, screenHeight : CGFloat) -> (CGFloat, CGFloat)
    {
        let cameraRatio : CGFloat = cameraHeight / cameraWidth
        let screenRatio : CGFloat = screenHeight / screenWidth
        
        var viewWidth : CGFloat = screenWidth
        var viewHeight : CGFloat = screenHeight
        
        if cameraRatio > screenRatio
        {
            viewHeight = viewHeight * cameraRatio / screenRatio
        }
        else
        {
            viewWidth = viewWidth * screenRatio / cameraRatio
        }
        return (viewWidth, viewHeight)
    }
    
    class func imageWithColor (color : UIColor) -> UIImage
    {
        let rect : CGRect = CGRect(x: 0, y: 0, width: 1.0, height: 1.0)
        UIGraphicsBeginImageContext(rect.size)
        let context : CGContext = UIGraphicsGetCurrentContext()!
        
        context.setFillColor(color.cgColor)
        context.fill(rect)
        
        let image: UIImage = UIGraphicsGetImageFromCurrentImageContext()!
        UIGraphicsEndImageContext()
        return image
    }
    
    class func radiansFromDegrees (degrees : CGFloat) -> CGFloat
    {
        return degrees * 2.0 * .pi / 360.0;
    }
}
