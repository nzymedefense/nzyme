import argparse

def sliceToJava(hexSlice, name):
  i = 0
  x = "public static final byte[] " + name + " = new byte[]{"
  for s in hexSlice:
    x = x + "(byte) 0x" + s

    if (i<len(hexSlice)-1):
      x = x + ", "
    
    i = i+1

  return x + "};"

parser = argparse.ArgumentParser()
parser.add_argument('-p', '--hexpayload', required=True)
parser.add_argument('-n', '--name', required=True)
args = parser.parse_args()

s = args.hexpayload.replace(" " , "")
name = args.name.upper()

payload = [s[i:i + 2] for i in range(0, len(s), 2)]

print sliceToJava(payload, name + "_" + "PAYLOAD")
