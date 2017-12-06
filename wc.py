import glob
import codecs
filelist = glob.glob("*.log")

dic={"AI WON":0,"AI LOSE":0,"DRAW":0}
for fname in filelist:
    file = codecs.open(fname, "r", "utf-8")
    lines = file.readlines()
    file.close()
    result = lines[-1].rstrip("\n").rstrip("\r")
    print(result)
    dic[result]+=1
print(dic)
pw = dic["AI WON"]/(dic["AI WON"]+dic["AI LOSE"]+dic["DRAW"])
print(pw)
