# Peristyle

Simple wallpaper manager app for Android.

## Why Peristyle?

Peristyle is created to be an extremely simple and sophisticated wallpaper manager and browser app for Android. It solves the problem of bloated apps with too many features which have very minimal support for locally stored wallpapers. You just wanted an app that allows you to browse, select, manage, and set your own locally stored wallpapers? Then Peristyle is for you :)

## Features

- Simple architecture. Browse images and use the system wallpaper manager to set them as wallpapers.
- Multiple folders support.
- Ability to assign Tags to any wallpaper.
- Can scan .nomedia directories. Useful if you want to keep your wallpapers out of your gallery.
- Apply blur and color filters dynamically on any wallpaper before applying.
- Simple yet pretty animations with proper optimizations.
- Compress or reduce images on the fly.
- No ads, no tracking, no analytics, no internet permissions, no unnecessary permissions.
- Auto wallpaper change support with dedicated folders and tags for each screen.
- Change wallpaper using app's live wallpaper
- Edit and apply filters on wallpapers losslessly in realtime.
- Built-in live wallpaper picker.
- Built-in Wallhaven client for browsing and downloading wallpapers from the internet.
- Dark mode support.
- Glassmorphic UI based on realtime blur effects and caustic shadows.
- Material You color theme.
- Fully reproducible build.
- Zero loading software architecture.

## Stats

![GitHub all releases](https://img.shields.io/github/downloads/Hamza417/Peri/total?label=Total%20Downloads&color=white)

## Download

[![](https://img.shields.io/github/v/release/Hamza417/Peristyle?color=181717&logo=github&label=GitHub%20Release)](https://github.com/Hamza417/Peristyle/releases/latest)
[![](https://img.shields.io/f-droid/v/app.simple.peri?logo=fdroid&logoColor=white&label=F-Droid&color=1976D2)](https://f-droid.org/en/packages/app.simple.peri/)
[![](https://img.shields.io/endpoint?url=https://apt.izzysoft.de/fdroid/api/v1/shield/app.simple.peri&logo=fdroid)](https://apt.izzysoft.de/fdroid/index/apk/app.simple.peri/)

## Screenshots

| ![01](./fastlane/metadata/android/en-US/images/phoneScreenshots/01.png) | ![02](./fastlane/metadata/android/en-US/images/phoneScreenshots/02.png) | ![03](./fastlane/metadata/android/en-US/images/phoneScreenshots/03.png) |
|:-----------------------------------------------------------------------:|:-----------------------------------------------------------------------:|:-----------------------------------------------------------------------:|
| ![04](./fastlane/metadata/android/en-US/images/phoneScreenshots/04.png) | ![05](./fastlane/metadata/android/en-US/images/phoneScreenshots/05.png) | ![06](./fastlane/metadata/android/en-US/images/phoneScreenshots/06.png) |
| ![07](./fastlane/metadata/android/en-US/images/phoneScreenshots/07.png) | ![08](./fastlane/metadata/android/en-US/images/phoneScreenshots/08.png) | ![09](./fastlane/metadata/android/en-US/images/phoneScreenshots/09.png) |
| ![10](./fastlane/metadata/android/en-US/images/phoneScreenshots/10.png) | ![11](./fastlane/metadata/android/en-US/images/phoneScreenshots/11.png) | ![12](./fastlane/metadata/android/en-US/images/phoneScreenshots/12.png) |

## Triggering AutoWallpaperService externally

Peristyle supports triggering the AutoWallpaperService externally using the following intent: `app.peristyle.START_AUTO_WALLPAPER_SERVICE`

**_You can use any automation or scheduling tool and create your own scenarios to change the wallpaper for any custom event such as locking, unlocking, etc._**

## Permission Usage

Peristyle needs `MANAGE_EXTERNAL_STORAGE` and `READ_MEDIA_IMAGES` to be allowed to show the system wallpapers in the app.
This has been discussed in [Issue #72](https://github.com/Hamza417/Peristyle/issues/72#issuecomment-2357558761).

The `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission is used to run the AutoWallpaperService whenever required.

Additionally requires access to any wallpaper directories the user specifies.

## Translate

[![Crowdin](https://badges.crowdin.net/peristyle/localized.svg)](https://crowdin.com/project/peristyle)

Peristyle supports localization. If you want to translate Peristyle into your own language(s), you can do so [here on Crowdin](https://crowdin.com/project/peristyle).

[Contributors](https://crowdin.com/project/peristyle/members)

## License

```
Copyright 2023 Hamza Rizwan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
