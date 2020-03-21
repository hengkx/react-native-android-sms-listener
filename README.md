## `react-native-android-sms-listener2` [![react-native-android-sms-listener2](https://badge.fury.io/js/react-native-android-sms-listener2.svg)](https://badge.fury.io/js/react-native-android-sms-listener2)

A utility that allows you to listen for incoming SMS messages.[fork](https://github.com/andreyvital/react-native-android-sms-listener)

- add long sms support
- add background sms
- add sim slot

### Example

```JS
import SmsListener from 'react-native-android-sms-listener2'

SmsListener.addListener(message => {
  console.info(message)
})
```

The contents of `message` object will be:

```JS
{
  originatingAddress: string,
  body: string
}
```

`SmsListener#addListener` returns a `CancellableSubscription` so if you want to stop listening for incoming SMS messages you can simply `.remove` it:

```JS
let subscription = SmsListener.addListener(...)

subscription.remove()
```

##### Example of using it for verification purposes:

...and if in your sign up process you have the phone number verification step which is done by sending a code via SMS to the specified phone, you might want to verify it automatically when the user receive it &mdash; pretty much like what Telegram or WhatsApp does:

```JS
let subscription = SmsListener.addListener(message => {
  let verificationCodeRegex = /Your verification code: ([\d]{6})/

  if (verificationCodeRegex.test(message.body)) {
    let verificationCode = message.body.match(verificationCodeRegex)[1]

    YourPhoneVerificationApi.verifyPhoneNumber(
      message.originatingAddress,
      verificationCode
    ).then(verifiedSuccessfully => {
      if (verifiedSuccessfully) {
        subscription.remove()
        return
      }

      if (__DEV__) {
        console.info(
          'Failed to verify phone `%s` using code `%s`',
          message.originatingAddress,
          verificationCode
        )
      }
    })
  }
})
```

If you're using Twilio or a similar third-party messaging service which you have a fixed phone number to deliver messages you might want to ensure that the message comes from your service by checking `message.originatingAddress`.

### Installation

```SH
$ npm install --save react-native-android-sms-listener2
$ react-native link react-native-android-sms-listener2
```

### Manual Installation

For a manual installation, all you need to do to use this so-called utility is:

_android/settings.gradle_

```Gradle
include ':react-native-android-sms-listener2'
project(':react-native-android-sms-listener2').projectDir = new File(rootProject.projectDir,'../node_modules/react-native-android-sms-listener/android')
```

_android/app/build.gradle_

```Gradle
dependencies {
  compile project(':react-native-android-sms-listener2')
  // (...)
}
```

_MainApplication.java_

```Java
import com.centaurwarchief.smslistener.SmsListenerPackage;
```

```Java
@Override
protected List<ReactPackage> getPackages() {
  return Arrays.<ReactPackage>asList(
    new MainReactPackage(),
    new SmsListenerPackage()
    // (...)
  );
}
```
