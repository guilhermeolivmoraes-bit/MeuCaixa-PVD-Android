import os
import json

extensions_dir = os.path.expanduser('~/.antigravity/extensions')

for folder in os.listdir(extensions_dir):
    package_json_path = os.path.join(extensions_dir, folder, 'package.json')
    if os.path.exists(package_json_path):
        try:
            with open(package_json_path, 'r') as f:
                data = json.load(f)
            
            modified = False
            if 'engines' in data and 'vscode' in data['engines']:
                if data['engines']['vscode'].startswith('^1.'):
                    data['engines']['vscode'] = '*'
                    modified = True
            
            if modified:
                with open(package_json_path, 'w') as f:
                    json.dump(data, f, indent=2)
                print(f"Patched {folder}")
        except Exception as e:
            print(f"Error patching {folder}: {e}")
