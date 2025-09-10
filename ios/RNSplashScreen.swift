/**
 * SplashScreen
 * from：http://attarchi.github.io
 * Author: Attarchi
 * GitHub: https://github.com/attarchi
 * Email: attarchi@me.com
 * Swift version by: React Native Community
 */

import UIKit
import React
import Lottie

@objc(SplashScreen)
public class SplashScreen: NSObject, RCTBridgeModule {
    
    // MARK: - Static Properties
    private static var forceToCloseByHideMethod = false
    private static var loadingView: UIView?
    private static var isAnimationFinished = false
    private static var window: UIWindow?
    
    // MARK: - RCTBridgeModule
    public static func moduleName() -> String! {
        return "SplashScreen"
    }
    
    public func methodQueue() -> DispatchQueue! {
        return DispatchQueue.main
    }
    
    @objc public static func setupLottieSplash(in window: UIWindow?, lottieName: String, backgroundColor: UIColor = UIColor.white, forceToCloseByHideMethod: Bool = false) {
        guard let rootViewController = window?.rootViewController,
              let rootView = rootViewController.view else { return }
        
        self.window = window
        self.forceToCloseByHideMethod = forceToCloseByHideMethod
        self.isAnimationFinished = false
        
        rootView.backgroundColor = backgroundColor
        
        let animationView = LottieAnimationView(name: lottieName)
        animationView.frame = rootView.frame
        animationView.center = rootView.center
        animationView.backgroundColor = backgroundColor

        showLottieSplash(animationView, inRootView: rootView)
    }
    
    @objc public static func setupCustomLottieSplash(in window: UIWindow?,  animationView: UIView, inRootView rootView: UIView, forceToCloseByHideMethod: Bool = false) {
        self.window = window
        self.forceToCloseByHideMethod = forceToCloseByHideMethod
        self.isAnimationFinished = false
        
        showLottieSplash(animationView, inRootView: rootView)
    }

    @objc private static func showLottieSplash(_ animationView: UIView, inRootView rootView: UIView) {
        loadingView = animationView
        isAnimationFinished = false
        
        // Ensure splash screen appears on top of React Native screen
        rootView.addSubview(animationView)
        rootView.bringSubviewToFront(animationView)
        
        // Set higher z-index to ensure splash screen stays on top
        animationView.layer.zPosition = 1000
        
        // Temporarily raise the window level to ensure it stays on top
        let originalWindowLevel = window?.windowLevel
        window?.windowLevel = UIWindow.Level.alert + 1
        
        // Play animation and handle completion
        if let lottieView = animationView as? LottieAnimationView {
            lottieView.play { finished in
                DispatchQueue.main.async {
                    isAnimationFinished = true
                    // Only auto-hide if not forced to keep splash until explicit hide() is called
                    if !forceToCloseByHideMethod {
                        hideSplashScreen()
                    }
                }
            }
        } else {
            // Fallback for non-Lottie views
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                isAnimationFinished = true
                if !forceToCloseByHideMethod {
                    hideSplashScreen()
                }
            }
        }
    }
    

    
    
    @objc public static func hide() {
        // Only hide if forceToCloseByHideMethod is true
        if forceToCloseByHideMethod {
            hideSplashScreen()
        }
    }
    
    private static func hideSplashScreen() {
        guard let loadingView = loadingView else { return }
        
        DispatchQueue.main.async {
            UIView.animate(withDuration: 0.2, animations: {
                loadingView.alpha = 0.0
            }, completion: { _ in
                loadingView.removeFromSuperview()
                self.loadingView = nil
                
                // Restore original window level
                window?.windowLevel = UIWindow.Level.normal
            })
        }
    }
    
    
    @objc public static func jsLoadError(_ notification: Notification) {
        // If there was an error loading javascript, hide the splash screen so it can be shown.  Otherwise
        // the splash screen will remain forever, which is a hassle to debug.
        hideSplashScreen()
    }
    
    // MARK: - Bridge Method
    @objc public func hide() {
        SplashScreen.hide()
    }
}

// MARK: - Module Registration
extension SplashScreen {
    @objc public static func requiresMainQueueSetup() -> Bool {
        return true
    }
}
