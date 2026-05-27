import os
import shutil

base_dir = '/home/guilherme/antigravity-workspace/MeuCaixa-PVD-Android/app/src/main/java/com/oliveira/meucaixa'
res_dir = '/home/guilherme/antigravity-workspace/MeuCaixa-PVD-Android/app/src/main/res'
manifest_path = '/home/guilherme/antigravity-workspace/MeuCaixa-PVD-Android/app/src/main/AndroidManifest.xml'

# mappings for package string replacements
replacements = {
    'com.oliveira.meucaixa.data.model': 'com.oliveira.meucaixa.models',
    'com.oliveira.meucaixa.data.local': 'com.oliveira.meucaixa.database',
    'com.oliveira.meucaixa.data.repository': 'com.oliveira.meucaixa.services',
    'com.oliveira.meucaixa.ui.sales.CartItem': 'com.oliveira.meucaixa.models.CartItem'
}

# files to move
moves = [
    ('data/model', 'models'),
    ('data/local', 'database'),
    ('data/repository', 'services'),
    ('ui/sales/CartItem.java', 'models/CartItem.java')
]

for src, dest in moves:
    src_path = os.path.join(base_dir, src)
    dest_path = os.path.join(base_dir, dest)
    
    # if it's a file, just move it
    if os.path.isfile(src_path):
        os.makedirs(os.path.dirname(dest_path), exist_ok=True)
        shutil.move(src_path, dest_path)
    # if it's a directory, move its contents
    elif os.path.isdir(src_path):
        os.makedirs(dest_path, exist_ok=True)
        for item in os.listdir(src_path):
            s = os.path.join(src_path, item)
            d = os.path.join(dest_path, item)
            shutil.move(s, d)

# Clean up empty data directories
for p in ['data/model', 'data/local', 'data/repository']:
    try:
        os.rmdir(os.path.join(base_dir, p))
    except OSError:
        pass
try:
    os.rmdir(os.path.join(base_dir, 'data'))
except OSError:
    pass

def replace_in_file(filepath):
    try:
        with open(filepath, 'r') as f:
            content = f.read()
            
        new_content = content
        for old, new in replacements.items():
            new_content = new_content.replace(old, new)
            
        if new_content != content:
            with open(filepath, 'w') as f:
                f.write(new_content)
    except Exception as e:
        print(f"Error processing {filepath}: {e}")

# Process all files in java, res, and manifest
for root_dir in [base_dir, res_dir]:
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith(('.java', '.kt', '.xml')):
                replace_in_file(os.path.join(dirpath, filename))

replace_in_file(manifest_path)

print("Refactoring complete.")
