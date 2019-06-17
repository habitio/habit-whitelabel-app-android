# habit-whitelabel-app-android

This repository contains the source code to build a Habit Whitelabel application for Android

This Application interacts with the Habit Platform API in order to:
* Create new user Accounts and Login
* Pair IoT devices available to a specific application. To have devices available on your specific application please get in touch by sending us an email to support@habit.io.
* List and interact with the user’s paired devices
* Create and manage the user’s Agent list. These Agents are automation rules that enables integrations between different manufacturer devices.
* Possible to enable/disable a messages screen, for you to be able to interact with your user.
* User’s profile screen, where the user can define his preferences.

We have an Analytics SDK that you can plugin with this project. In order to do so, get in touch with us by sending a mail to support@habit.io, and we will provide it.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

In order to interact with the Habit API, clients must authenticate using the OAuth 2.0 protocol, in particular the Resource Owner Password Credentials flow.

This project already implements the client side of the OAuth2.0 flow, you just need to configure your client-id in “app/src/muzzley/res/values/strings_services.xml” field “app_client_id”

To receive your client-id please get in touch with us by sending us an email to support@habit.io

Add your own app/google-services.json (obtained from your Firebase Console).

Rename package name to match your google-services.json

Search project source files for string "REPLACEME" and replace it with your own information. Here are some examples:
* “azure_connection_string” - this project receives push notification sent with the Azure provider. For you to have access to the feature you must have an active Azure account and configure a notification hub. Afterwards, your notification subscription endpoint should be placed here. Get in touch with us to be able to setup your publishing notification endpoint.
* ”app_client_id” - your application client-id to be used during the OAuth2.0 flow
* ”google_maps_web_key” - a google maps key, compatible with Web, because it will be used on some webviews implemented in the application

It's now ready to be compiled with Android Studio, or on the command line.

### Customizations

There are additional properties you can tweak to your requirements that are described [here](doc/properties.md)

## Installing

Install the latest Android Studio


## Deployment
```
./gradlew installMuzzleyDebug
```
Instead of Debug you can also use targets Integrations, Staging, Qa, Production, Release

For release builds you need to create a muzzley.properties in the same format as debug.properties


## Built With

Android Studio 3.4.1 and bundled openjdk version "1.8.0_152-release"


## Versioning

For the versions available, see the [tags on this repository](https://github.com/habitio/habit-whitelabel-app-android/tags).

## Authors
See the list of [contributors](https://github.com/habitio/habit-whitelabel-app-android/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
