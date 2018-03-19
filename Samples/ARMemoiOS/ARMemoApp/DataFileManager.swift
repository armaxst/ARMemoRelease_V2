//
//  FileManager.swift
//  ARMemoApp
//
//  Created by 이상훈 on 2018. 2. 21..
//  Copyright © 2018년 Ray. All rights reserved.
//

import Foundation

var fileDirectory : String? = nil
let fileManager = FileManager.default

class DataFileManager
{
    class func checkDataDirectory () -> Bool
    {
        let documentPath : String = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
        
        let directoryFullPath : String = documentPath + "/Data"
        
        if fileManager.fileExists(atPath: directoryFullPath)
        {
            do {
                try fileManager.removeItem(atPath: directoryFullPath)
            }
            catch {
                return false
            }
        }
        
        do {
            try fileManager.createDirectory(atPath: directoryFullPath, withIntermediateDirectories: true, attributes: nil)
        }
        catch {
            return false
        }
        
        fileDirectory = directoryFullPath
        return true
    }
    
    class func existDataFile () -> Bool
    {
        let trackingfileFullPath : String = fileDirectory! + "/Trackable.armemo"
        let strokefileFullPath : String = fileDirectory! + "/Stroke.txt"

        if fileManager.fileExists(atPath: trackingfileFullPath) && fileManager.fileExists(atPath: strokefileFullPath)
        {
            return true
        }
        else
        {
            return false
        }
    }
    
    class func saveStrokeData (imageWidth :Int32, imageHeight : Int32, object : Any) -> Bool
    {
        let fileFullPath : String = fileDirectory! + "/Stroke.txt"
        if fileManager.fileExists(atPath: fileFullPath)
        {
            do {
                try fileManager.removeItem(atPath: fileFullPath)
            }
            catch {
                print("Stroke File Remove Failed")
            }
        }
        
        let fileURL : URL = URL.init(fileURLWithPath: fileFullPath)
        
        var jsonDic : [String : Any] = NSMutableDictionary() as! [String : Any];
        jsonDic["imageWidth"] = imageWidth
        jsonDic["imageHeight"] = imageHeight
        jsonDic["strokes"] = object

        let jsonData = try? JSONSerialization.data(withJSONObject: jsonDic, options: [])
        let jsonString : String = String(data: jsonData!, encoding: .utf8)!
        
        do {
            try jsonString.write(to: fileURL as URL, atomically: false, encoding: .utf8)
        }
        catch {
            print("Stroke file save failed")
        }
        
        return true
    }
    
    class func loadStrokeData () -> NSDictionary
    {
        let fileFullPath : String = fileDirectory! + "/Stroke.txt"
        let fileURL : URL = URL.init(fileURLWithPath: fileFullPath)
        
        var jsonString = ""
        do {
            jsonString = try String(contentsOf: fileURL)
        } catch {
            print("Failed reading from URL: \(fileURL), Error: " + error.localizedDescription)
        }
        
        let data = jsonString.data(using: .utf8)
        
        let jsonDic : NSDictionary = try! JSONSerialization.jsonObject(with: data!, options: []) as! NSDictionary
        return jsonDic
    }
    
    class func getTrackableDataPath () -> String?
    {
        return fileDirectory! + "/Trackable.armemo"
    }
}

