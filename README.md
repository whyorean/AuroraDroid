<img src="https://i.imgur.com/kSApIjL.png" height="256" alt="Aurora Logo"><br/><img src="https://www.gnu.org/graphics/gplv3-88x31.png" alt="GPL v3 Logo">

# AuroraDroid: An FDroid Client

*Aurora Droid* is an unofficial, FOSS client to FDroid and FDroid-compatible repositories with an elegant design. Not only does Aurora Store download, update, and search for apps like the Official FDroid client, it also empowers the user with new features.

AuroraDroid, while being a new project, also shares some code with the official FDroid client.

# Features

* Free/Libre software
  -- Has GPLv3 licence

* Beautiful design
  -- Built upon latest Material Design guidelines

* Download manager
  -- To manage multiple downloads along with pause, resume and retry functionalities.

* Inbuilt repo list
  -- You can add your own repos, but most known repositories are already included and you just need to enable them on startup. No more wasting finger power!

* Transparent downloads
  -- You are free to download any (even unsupported) versions of an application and they are marked clearly with their architectures and minimum SDKs, unlike the official clients.

# Screenshots

<img src="https://gitlab.com/AuroraOSS/AuroraDroid/raw/master/fastlane/metadata/android/en-US/phoneScreenshots/ss001.png" height="400"><img src="https://gitlab.com/AuroraOSS/AuroraDroid/raw/master/fastlane/metadata/android/en-US/phoneScreenshots/ss002.png" height="400">
<img src="https://gitlab.com/AuroraOSS/AuroraDroid/raw/master/fastlane/metadata/android/en-US/phoneScreenshots/ss003.png" height="400"><img src="https://gitlab.com/AuroraOSS/AuroraDroid/raw/master/fastlane/metadata/android/en-US/phoneScreenshots/ss004.png" height="400">
<img src="https://gitlab.com/AuroraOSS/AuroraDroid/raw/master/fastlane/metadata/android/en-US/phoneScreenshots/ss005.png" height="400"><img src="https://gitlab.com/AuroraOSS/AuroraDroid/raw/master/fastlane/metadata/android/en-US/phoneScreenshots/ss006.png" height="400">

# Frequently Asked Questions

* What is the difference between AuroraDroid and the official FDroid client?

  It isn't a buggy mess. Of course, alongwith the download manager, transparent downloads and MD2 design.

* Why does this even need the camera permissions?

  Many repositories provide a QR code for their links and AuroraDroid can scan them directly. If you don't want to use this feature, you don't need to give it permissions. You can always add links by hand.

* Why is there WhatsApp/Skype/miscellaneous-proprietary-app in the apps list? This is blasphemy!

  Why do you think? Many repos (including Haagch and IzzyDroid) are known to include proprietary/non-free applications. All of them are disabled at start, so it's on you for enabling them without knowing them.

* How does AuroraDroid install apps?

  AuroraDroid can install apps in 3 ways:
    * Manual - Whenever an app is downloaded, it will open the manual installation screen. This doesn't require root or system perms.
    * Root/System - By giving AuroraDroid root or system permissions, it will automatically install apps in the background as soon as they are downloaded.
    * Aurora Services - By installing Aurora Services as system app, AuroraDroid can automatically install app upon download completion in background.

* How do I use Aurora Services?

  1. Install Aurora Services (preferably to the system).
  2. Open Aurora Services and follow the initial set-up instructions
  3. Open Aurora Services' settings and choose Aurora Services it as an install method.

  You don't need to give AuroraDroid system or root grants; Aurora Services handles all install and uninstall requests in the background.
  
* How to give AuroraDroid/Services system permissions?

  You need to either manually push the APKs to `/system/priv-app`, or install the [Magisk](https://gitlab.com/AuroraOSS/AuroraServices/-/tags) module from the services release page.

* Why are the versions on F-Droid and XDA labs outdated? When will they be updated?

  AuroraDroid is still in a development phase right now; Only infrequent, stable builds will be uploaded there. F-Droid's review & build process is also quite lengthy. <br/>
  You can always grab the latest tests builds either from the [Telegram Group](https://t.me/AuroraDroid) or from [AuroraOSS](http://auroraoss.com/Nightly/)
  
# Links
* AndroidFileHost - [Downloads](https://androidfilehost.com/?w=files&flid=294487)
* XDA Forum - [Thread](https://forum.xda-developers.com/android/apps-games/app-aurora-droid-fdroid-client-t3932663)
* XDA Labs - [Link](https://labs.xda-developers.com/store/app/com.aurora.adroid)
* Support Group - [Telegram](https://t.me/AuroraDroid)

# Aurora Droid uses the following Open Source libraries:

* [RX-Java](https://github.com/ReactiveX/RxJava)
* [ButterKnife](https://github.com/JakeWharton/butterknife)
* [OkHttp3](https://square.github.io/okhttp/)
* [Glide](https://github.com/bumptech/glide)
* [Fetch2](https://github.com/tonyofrancis/Fetch)

