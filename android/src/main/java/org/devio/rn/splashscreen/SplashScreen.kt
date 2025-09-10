/**
 * SplashScreen
 * from：http://attarchi.github.io
 * Author: Attarchi
 * GitHub: https://github.com/attarchi
 * Email: attarchi@me.com
 * Swift version by: React Native Community
 */

package org.devio.rn.splashscreen

import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.os.Build
import com.airbnb.lottie.LottieAnimationView
import java.lang.ref.WeakReference

object SplashScreen {
    private var mSplashDialog: Dialog? = null
    private var mActivity: WeakReference<Activity>? = null
    private var isAnimationFinished = false
    private var waiting = false
    private var forceToCloseByHideMethod = false
    private var animationStartTime: Long = 0


    fun show(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int, forceToCloseByHideMethod: Boolean = false) {
        if (activity == null) {
            println("SplashScreen: ERROR - Activity is null")
            return
        }
        mActivity = WeakReference(activity)
        this.forceToCloseByHideMethod = forceToCloseByHideMethod
        this.isAnimationFinished = false
        
        activity.runOnUiThread {
            if (!activity.isFinishing) {
                mSplashDialog = Dialog(activity, themeResId)
                mSplashDialog?.setContentView(R.layout.launch_screen)
                mSplashDialog?.setCancelable(false)
                val lottie = mSplashDialog?.findViewById<LottieAnimationView>(lottieId)

                // Configure Lottie animation to play once and not loop
                // These settings will override any XML configuration
                lottie?.repeatCount = 0
                lottie?.speed = 1.0f
                
                // Ensure animation is stopped before starting programmatically
                lottie?.cancelAnimation()
                lottie?.progress = 0f


                lottie?.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        println("SplashScreen: Animation started")
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        val animationDuration = (System.currentTimeMillis() - animationStartTime) / 1000.0
                        setAnimationFinished(true)

                        // Only auto-hide if not forced to keep splash until explicit hide() is called
                        if (!forceToCloseByHideMethod) {
                            if (animationDuration > 0.5) {
                                hideSplashScreen()
                            } else {
                                // Wait for minimum duration
                                lottie?.postDelayed({
                                    hideSplashScreen()
                                }, ((2.0 - animationDuration) * 1000).toLong())
                            }
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        println("SplashScreen: Animation was cancelled")
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        println("SplashScreen: Animation repeated (this shouldn't happen)")
                    }
                })
                
                // Start the animation manually after a small delay
                lottie?.postDelayed({
                    animationStartTime = System.currentTimeMillis()
                    lottie?.playAnimation()
                }, 100)

                if (mSplashDialog?.isShowing == false) {
                    mSplashDialog?.show()
                }
            }
        }
    }

    @JvmStatic
    fun setAnimationFinished(flag: Boolean) {
        isAnimationFinished = flag
    }

    fun hide(activity: Activity?) {
        // Only hide if forceToCloseByHideMethod is true
        if (forceToCloseByHideMethod) {
            hideSplashScreen()
        }
    }
    
    private fun hideSplashScreen() {
        var _activity = mActivity?.get()
        if (_activity == null) {
            return
        }

        _activity.runOnUiThread {
            if (mSplashDialog != null && mSplashDialog?.isShowing == true) {
                var isDestroyed = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    isDestroyed = _activity.isDestroyed
                }

                if (!_activity.isFinishing && !isDestroyed) {
                    mSplashDialog?.dismiss()
                    mSplashDialog = null
                    waiting = true
                }
            }
        }
    }
    
    @JvmStatic
    fun setForceToCloseByHideMethod(flag: Boolean) {
        forceToCloseByHideMethod = flag
    }
}
