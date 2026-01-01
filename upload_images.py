import os
import mysql.connector

BASE_DIR = "resources/images"

FRUITS_DIR = os.path.join(BASE_DIR, "fruits")
VEGETABLES_DIR = os.path.join(BASE_DIR, "vegetables")

def load_image(path):
    with open(path, "rb") as f:
        return f.read()

conn = mysql.connector.connect(
    host="localhost",
    user="myuser",
    password="1234",
    database="greengrocer_group4"
)

cursor = conn.cursor()

# FRUITS
for filename in os.listdir(FRUITS_DIR):
    name = os.path.splitext(filename)[0]  # apple.jpg -> apple
    image_data = load_image(os.path.join(FRUITS_DIR, filename))
    cursor.execute(
        "UPDATE productinfo SET image_blob = %s WHERE LOWER(name) = LOWER(%s)",
        (image_data, name)
    )
    print(f"✅ fruit: {name} (rows updated: {cursor.rowcount})")
    conn.commit()

# VEGETABLES
for filename in os.listdir(VEGETABLES_DIR):
    name = os.path.splitext(filename)[0]
    image_data = load_image(os.path.join(VEGETABLES_DIR, filename))
    cursor.execute(
        "UPDATE productinfo SET image_blob = %s WHERE LOWER(name) = LOWER(%s)",
        (image_data, name)
    )
    print(f"✅ vegetable: {name} (rows updated: {cursor.rowcount})")
    conn.commit()

conn.commit()
cursor.close()
conn.close()

print("\n🎉 ALL IMAGES LOADED")

