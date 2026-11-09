# Firebase: aapki taraf se kya chahiye

Deployment abhi band hai. APK bina Firebase details ke build/install ho sakti hai aur setup screen dikhayegi; owner login aur market sync ke liye real configuration zaroori hai.

## Abhi ke liye

1. Apne Google account se [Firebase Console](https://console.firebase.google.com/) open karein. Malik Oil ke liye project banayein ya existing project select karein. No-cost Spark plan kaafi hai; is step ke liye billing enable karne ki zarurat nahi.
2. Project Settings → Your apps → Add app → Android. Package name bilkul yeh rakhein: `com.malikoil.intelligence`. Nickname Malik Oil rakh sakte hain.
3. `google-services.json` download karein. Yeh Android app configuration hai, service-account private key nahi. File mujhe dein, ya permanent project mein rakh kar helper chalayein.
4. Authentication → Sign-in method mein Email/Password enable karein.
5. Authentication → Users mein apne liye owner account create karein. Strong password khud set karein. Users list se us account ka **UID** copy karke dein. Password mujhe na bhejein.

Sirf Android configuration aur UID local setup ke liye chahiye. Firebase project creation mein aapko apne account par login aur terms accept khud karne honge. Main aapka Google password nahi mangunga.

## Local configuration helper

Permanent project root se:

```powershell
python tools/configure_firebase.py "C:/actual/path/google-services.json" --owner-uid "ACTUAL_UID"
.\tools\build-apk.ps1
```

Helper correct Android package check karke ignored `android/firebase.local.properties` banata hai. Project ID aur UID backend configuration mein update hote hain. Woh Firebase project create, cloud resource provision ya deploy nahi karta. Credentials ke baghair default build mein koi fake UID, password ya API URL nahi hota.

Firebase app identifiers APK mein normally public hote hain. Backend owner UID verification data access protect karti hai. Service-account private key ko APK ya chat mein na rakhein.

## Baad mein, jab deployment approve ho

Cloudflare account/project aur API ka actual HTTPS URL, aur FCM HTTP v1 ke liye least-privilege service account ke server-only credentials chahiye honge. Abhi service-account key create/share karne ki zarurat nahi. Backend deploy na hone ki wajah se Firebase configure karne ke baad bhi mandi rates sync aur cloud notifications chalna shuru nahi honge.

## Official references

- [Firebase Android app registration and config download](https://firebase.google.com/docs/android/setup)
- [Enable email/password authentication](https://firebase.google.com/docs/auth/android/password-auth)
- [Firebase pricing](https://firebase.google.com/pricing)
