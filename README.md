<img align="left" width="85" height="85" src="https://raw.githubusercontent.com/lppedd/idea-conventional-commit/master/images/cc_logo.png" alt="Plugin logo">

# Conventional Commit

### Available @ [JetBrains Plugins Repository][1]

Looking for the latest **plugin binaries**? Get them [here][2] as `.zip`  
<small>Supported IDE versions: `192.*` to `193.*` (both inclusive)</small> 

-----

The aim of this plugin is to provide completion for [conventional commits][3],
also named _semantic_ commits, inside the VCS Commit dialog.  
The plugin must provide:

 - standard completion - based on context
 - template completion - initiated intentionally
 - extensibility - which means allowing attaching providers for the various parts of the commit:  
   _type_, _scope_ and _subject_

-----

Writing quality commit messages is important to keep an understandable and searchable history
of your project. Conventional commits are a perfect example of that.  
However it can happen choosing the correct _type_ or _scope_ (if any) isn't that immediate.
We might have forgotten about when a specific _type_ should be used, or which are the
available _scopes_, or we simply need a way to quickly complete the _subject_.

The plugin helps with the above, while also respecting the aforementioned requirements, so that
each user is able to customize the experience based on its preferences. 

### Completion modes

The plugin offers two completion modes.
  
  - #### Context based
    The commit message can be written like you have done until now, but by explicitly invoking
    completion you'll be offered the correct items based on the scope.
    Certain commodities, such as auto-completing the scope parenthesis or the `:` separator,
    are there too.
    
    <img width="845" height="229" src="https://raw.githubusercontent.com/lppedd/idea-conventional-commit/master/images/cc_standard.gif" alt="Standard completion">
    
  - #### Template based
    By firstly invoking the _type_ completion and confirming an item, a template will be
    generated in place and you'll be guided through each token (_scope_ and _subject).
    You can also go back (with `shift + tab`) and change your pick.
    
    Arbitrary characters insertion is also allowed inside each template's range marker.

    <img width="845" height="229" src="https://raw.githubusercontent.com/lppedd/idea-conventional-commit/master/images/cc_template.gif" alt="Template completion">

### Custom default tokens

Default tokens can be totally customized, and shared with your team, by creating and populating
a JSON file named 
```
cc_defaults.json
```

<img width="845" height="528" src="https://raw.githubusercontent.com/lppedd/idea-conventional-commit/master/images/cc_custom_defaults.png" alt="Custom defaults">

You're allowed to provide custom _types_ and _scopes_, and the JSON file must respect a certain scheme, 
which is shown here as an example.

```json
{
  "customType": {
    "description":"My custom type description"
  },
  "anotherCustomType": { },
  "yetAnotherCustomType": {
    "description":"This is my description",
    "scopes": {
      "first": {
        "description":"My first description"
      },
      "second": {
        "description":"My second description"
      }
    }
  }
}
```

### Providers

In a fresh installation you'll only be offered the most common tokens (e.g. `fix`, `feat`, `build`, etc.),
but the plugin exposes an API to enhance completion items, per each token.
_Type_, _scope_ and _subject_ each have a specific entry point, and the implementation is called
**Provider**. Each Provider is listed in a table, based on its context.

<img width="845" height="503" src="https://raw.githubusercontent.com/lppedd/idea-conventional-commit/master/images/cc_providers.png" alt="Providers">

You're allowed to re-order Providers per your necessities, knowing that possible duplicates
are going to be automatically filtered by the core engine.

<b>More on this later on...</b>

-----

## Author

 - Edoardo Luppi (<lp.edoardo@gmail.com>)

[1]: https://plugins.jetbrains.com/plugin/13389-conventional-commit
[2]: https://github.com/lppedd/idea-conventional-commit/releases
[3]: https://conventionalcommits.org/
