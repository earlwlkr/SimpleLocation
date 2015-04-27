# SimpleLocation
Demo cho bài seminar phát triển ứng dụng di động với đề tài Location

## Getting started

1. Download Google Play services from SDK Manager.
2. Go to https://console.developers.google.com, create a project if you haven't.
3. Go to **APIs and auth**, enable the following APIs:
  * Google Maps Android API v2
  * Directions API
  * Places API for Android
  * Distance Matrix API
4. Go to **Credentials** and retrieve the API key.
5. Edit the *geo_api_key* value in *apikeys.xml* to yours.
6. Build and run project.

## Contributing

Before you push to the remote repository, make sure you don't commit changes to *apikeys.xml* by doing the following:

```
git update-index --assume-unchanged app\src\main\res\values\apikeys.xml
```

## License

MIT License
