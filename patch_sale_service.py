import re

with open("app/src/main/java/com/oliveira/meucaixa/services/SaleService.java", "r") as f:
    content = f.read()

test_constructor = """
    // Construtor para testes unitários
    public SaleService(AppDatabase db, SaleDao saleDao, ProductDao productDao, IngredientDao ingredientDao, ProductIngredientDao productIngredientDao, ExecutorService executorService) {
        this.db = db;
        this.saleDao = saleDao;
        this.productDao = productDao;
        this.ingredientDao = ingredientDao;
        this.productIngredientDao = productIngredientDao;
        this.executorService = executorService;
    }
"""

if "Construtor para testes unitários" not in content:
    content = content.replace("    public SaleService(Application application) {", test_constructor + "\n    public SaleService(Application application) {")
    with open("app/src/main/java/com/oliveira/meucaixa/services/SaleService.java", "w") as f:
        f.write(content)
