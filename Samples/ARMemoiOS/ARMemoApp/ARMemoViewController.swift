//
//  ARMemoViewController.swift
//  ARMemoApp
//
//  Created by 이상훈 on 2018. 2. 13..
//  Copyright © 2018년 Ray. All rights reserved.
//

import UIKit

class ARMemoViewController: UIViewController {

    @IBOutlet weak var segmentedControl: UISegmentedControl!
    @IBOutlet weak var resolutionLabel: UILabel!
    
    @IBOutlet weak var learnButton: UIButton!
    @IBOutlet weak var trackingButton: UIButton!
    
    @IBOutlet weak var versionText: UILabel!
    
    var width : String = "640"
    var height : String = "480"

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.navigationItem.title = "ARMEMO"
        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        
        let textAttributes = [NSAttributedStringKey.foregroundColor:UIColor.white]
        navigationController?.navigationBar.titleTextAttributes = textAttributes
        
        let result : Bool = DataFileManager.checkDataDirectory()
        if result == false
        {
            exit(0)
        }
        
        initUIObject()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.backgroundColor = UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0)
        self.navigationController?.navigationBar.setBackgroundImage(UIImage.init(), for: UIBarMetrics.default)
        self.navigationController?.navigationBar.shadowImage = UIImage.init()
        self.navigationController?.navigationBar.isTranslucent = true
        
        self.navigationController?.navigationBar.isHidden = false
        
        if width == "640"
        {
            segmentedControl.selectedSegmentIndex = 0
        }
        else if width == "1280"
        {
            segmentedControl.selectedSegmentIndex = 1
        }
        else
        {
            segmentedControl.selectedSegmentIndex = 2
        }
        
        resolutionLabel.text = width + "X" + height;
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    private func initUIObject() {
        let versionString : String = Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String
        versionText.text = "v" + versionString
        
        learnButton.layer.borderWidth = 1.0
        learnButton.layer.borderColor = UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0).cgColor
        learnButton.layer.cornerRadius = CGFloat(roundf(Float(learnButton.frame.size.width / 2.0)))
        learnButton.layer.masksToBounds = true
        learnButton.setBackgroundImage(Utilites.imageWithColor(color: UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0)), for: UIControlState.highlighted)
        
        trackingButton.layer.borderWidth = 1.0
        trackingButton.layer.borderColor = UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0).cgColor
        trackingButton.layer.cornerRadius = CGFloat(roundf(Float(learnButton.frame.size.width / 2.0)))
        trackingButton.layer.masksToBounds = true
        trackingButton.setBackgroundImage(Utilites.imageWithColor(color: UIColor.init(red: 0, green: 195/255.0, blue: 179/255.0, alpha: 1.0)), for: UIControlState.highlighted)
    }
    
    @IBAction func indexChanged(_ sender: UISegmentedControl) {
        switch segmentedControl.selectedSegmentIndex {
        case 0:
            width = "640"
            height = "480"
        case 1:
            width = "1280"
            height = "720"
        case 2:
            width = "1920"
            height = "1080"
        default:
            ()
        }
        resolutionLabel.text = width + "X" + height;
    }
    
    @IBAction func clickLearning(_ sender: Any) {
        UserDefaults.standard.set(width, forKey: "LearnWidth")
        UserDefaults.standard.set(height, forKey: "LearnHeight")
        UserDefaults.standard.synchronize();
        
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
//        let LVC = storyboard.instantiateViewController(withIdentifier: "LearningViewController") as!
//        LearningViewController
        
        let LVC = storyboard.instantiateViewController(withIdentifier: "LearnViewController") as!
                LearnViewController

        navigationController?.pushViewController(LVC,
                                                 animated: true)
    }
    
    @IBAction func clickTracking(_ sender: Any) {
        if DataFileManager.existDataFile() == false
        {
            let alert = UIAlertController(title: "Error", message: "Don't find Tracking Data File", preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }
        else
        {
            UserDefaults.standard.set(width, forKey: "LearnWidth")
            UserDefaults.standard.set(height, forKey: "LearnHeight")
            UserDefaults.standard.synchronize();
            
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            let TVC = storyboard.instantiateViewController(withIdentifier: "TrackingViewController") as!
            TrackingViewController
            navigationController?.pushViewController(TVC,
                                                     animated: true)
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
