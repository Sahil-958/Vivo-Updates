import xml.etree.ElementTree as ET
try:
    tree = ET.parse('app/src/main/res/values/strings.xml')
    root = tree.getroot()
    for child in root:
        if 'name' in child.attrib:
            name = child.attrib['name']
            if ' ' in name:
                print(f"Found space in name: '{name}'")
            if not name.replace('_', '').isalnum():
                print(f"Found non-alnum in name: '{name}'")
except Exception as e:
    print(f"XML parse error: {e}")
