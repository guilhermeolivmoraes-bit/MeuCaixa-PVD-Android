import os

base_dir = '/home/guilherme/antigravity-workspace/MeuCaixa-PVD-Android/app/src/main/java/com/oliveira/meucaixa'

def replace_in_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()
    new_content = content
    for old, new in replacements:
        new_content = new_content.replace(old, new)
    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)

replacements = [
    ('IngredientRepository', 'IngredientService'),
    ('ingredientRepository', 'ingredientService'),
    ('com.oliveira.meucaixa.repositories.IngredientService', 'com.oliveira.meucaixa.services.IngredientService')
]

for root, _, files in os.walk(os.path.join(base_dir, 'ui')):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            replace_in_file(filepath, replacements)
