# JustPermissions
A permissions plugin for Minestom

Should be stable enough for a server

## Progress
- [x] player specific permissions
- [x] group specific permissions
- [x] permission saving
- [x] update player perms when changing group perms
- [ ] customizable messages?
- [x] more storage options (h2, sqlite etc)
- [ ] clean up the code
- [x] comment code
- [ ] allow importing of LuckPerm databases

Please report bugs in the issue tab

## Adding you permissions to the tab completion

If you want to add your extensions permissions to the tab completion
then add this to your extension.json
```json
"meta": {
    "permissions": [
      "yourextension.permissions",
      "ye.permissions"
    ]
  }
```
So your extension.json should now look like this
```json
{
  "entrypoint": "com.example.extensionname.Main",
  "name": "Extension",
  "version": "1.0.0",
  "meta": {
    "permissions": [
      "yourextension.permissions",
      "ye.permissions"
    ]
  }
}
```