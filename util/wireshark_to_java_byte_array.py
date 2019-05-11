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
parser.add_argument('-l', '--headerlen', type=int, required=True)
parser.add_argument('-p', '--hexpayload', required=True)
parser.add_argument('-n', '--name', required=True)
args = parser.parse_args()

s = args.hexpayload
headerlen = int(args.headerlen)*2
name = args.name.upper()

header = [s[i:i + 2] for i in range(0, headerlen, 2)]
payload = [s[i:i + 2] for i in range(headerlen, len(s), 2)]

print sliceToJava(header, name + "_" +"HEADER")
print sliceToJava(payload, name + "_" + "PAYLOAD")
