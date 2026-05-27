import os
import shutil

base_dir = '/home/guilherme/antigravity-workspace/MeuCaixa-PVD-Android/app/src/main/java/com/oliveira/meucaixa'
services_dir = os.path.join(base_dir, 'services')
repos_dir = os.path.join(base_dir, 'repositories')

os.makedirs(repos_dir, exist_ok=True)

# 1. Move repository files to the repositories directory
repo_files = ['SaleRepository.java', 'ProductRepository.java', 'IngredientRepository.java']
for r_file in repo_files:
    src = os.path.join(services_dir, r_file)
    dst = os.path.join(repos_dir, r_file)
    if os.path.exists(src):
        shutil.move(src, dst)

# 2. Update package name in the moved files
def replace_in_file(filepath, old_str, new_str):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        content = f.read()
    content = content.replace(old_str, new_str)
    with open(filepath, 'w') as f:
        f.write(content)

for r_file in repo_files:
    dst = os.path.join(repos_dir, r_file)
    replace_in_file(dst, 'package com.oliveira.meucaixa.services;', 'package com.oliveira.meucaixa.repositories;')

# 3. Create Service files in the services directory
# To simplify, we will just create Service wrappers that take Application context,
# initialize Repositories, and move the business logic from Repositories to Services.
# Actually, the user's code relies on repositories in ViewModels. 
# We need to change ViewModels to use Services instead of Repositories!

def find_and_replace_global(old_str, new_str):
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                replace_in_file(filepath, old_str, new_str)

# Instead of doing complex code parsing, let's just make the ViewModels import the repositories from the new package,
# and we create empty Service classes for the user to populate, OR we can move the exact logic.
# The user said: "se precisar crie uma pastas repositories para colocar as classes repository e criar classes services para colocar na pasta service"
# I will create the services and move the business logic there.

