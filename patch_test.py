import re

with open("app/src/test/java/com/oliveira/meucaixa/InventoryControlTest.java", "r") as f:
    content = f.read()

content = content.replace("SaleItem cartItem = new SaleItem();\n        cartItem.setProductId(productId);\n        cartItem.setQuantity(2.0); // Vendendo 2 unidades", 'SaleItem cartItem = new SaleItem(0L, productId, "Refrigerante", 10.0, 5.0, 2.0);')
content = content.replace("SaleItem cartItem = new SaleItem();\n        cartItem.setProductId(productId);\n        cartItem.setQuantity(2.0);", 'SaleItem cartItem = new SaleItem(0L, productId, "Bolo", 20.0, 0.0, 2.0);')
content = content.replace("SaleItem cartItem = new SaleItem();\n        cartItem.setProductId(productId);\n        cartItem.setQuantity(5.0); // Vendendo 5, mais do que tem no estoque!", 'SaleItem cartItem = new SaleItem(0L, productId, "Produto", 10.0, 5.0, 5.0);')

with open("app/src/test/java/com/oliveira/meucaixa/InventoryControlTest.java", "w") as f:
    f.write(content)
