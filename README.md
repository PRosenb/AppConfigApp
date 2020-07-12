# Android App Config App
https://github.com/PRosenb/AppConfigApp

This app belongs to the project [AppConfig](https://github.com/PRosenb/AppConfig), please also see details there.

## Config Entries
A config entry defines how an app under test is accessed and what key/values are sent to it.
In the `App Config` app, you can add a config entry as follows.
- On the main screen of `App Config` app, press on the `+` icon on bottom right to add a new entry.
- Enter name and authority and press the back key.

### Config Entry
- Name: A name displayed within `Config App` to identify the entry. It is not sent to the app under test.
- Authority: The authority of the `ContentProvider` of the app under test. This string must match exactly.
- Key/values: Key/Value pairs to be sent to the app under test (see below).

#### Key/Values
The key/values belong to a `Config Entry` and are sent to the app under test where they are set as `SharedPreferences` by default. You can specify as many key/values you like.

- Key: The key, by default used as `key` in `SharedPreferences` inside of the app under test.
- Value: The value to be set for the given key. It can also be `null`. When it's `null`, the given `key` is deleted by default in the app under test.

## External Config
The `External Config` feature allows you to centraly manage `App Config` app configurations that are automatically synced to all subscribed `App Config` apps.
So if there are multiple Devs, QAs and Product Managers on a project, a single person can manage the configuration of all the instances of `App Config` app within that project.

`External Config` requires a publicly accessible `https` URL where the `External Config` is fetched from by the `App Config` app. There, a `YAML` file is placed
that defines the configuration available to all subscribed users. The file format `YAML` was chosen to allow a build script to easily append new entries to it.

### Config File
Example `External Config` `YAML` file.
```YAML
- id: example-prod
  name: Example Prod
  authority: com.example.config
  keyValues:
    - key: START_URL
      value:
- id: example-staging
  name: Example Staging
  authority: com.example.config
  keyValues:
    - key: START_URL
      value: https://staging.example.com
```
The `id` identifies entries on the `App Config` app and allows to modify them over time. The other fields are identical to the `Config Entry` as described above.  
In the example above, the entry `example-staging` sets the `START_URL` to `https://staging.example.com` while the entry `example-prod` clears `START_URL` so that the app under test falls back to the default URL.  
You can spedify as many keys as you like.

### Configuration in App Config app
Use the following steps to subscribe an instance of `App Config` app to such an `External Config` URL.
- On main screen, press on the Cog Wheel icon on top right
- Press on the `+` icon on bottom right to add a new entry
- Type a name for the `External Config` like `Example App`
- Type the whole public URL including `https://`, e.g. `https://example.com/config.yaml`

## Contributions ##
Enhancements and improvements are welcome.

## License ##
``` text
Android AppConfigApp
Copyright (c) 2020 Peter Rosenberg (https://github.com/PRosenb).

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
