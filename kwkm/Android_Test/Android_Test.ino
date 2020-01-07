int a = 0;
int count = 0;

String data = "";
bool readComplete = false;
String inputMessage = "";

void setup() {
  Serial.begin(115200);
  //while(!Serial.available()){}
}

void loop() {
  if(readComplete){
    if(true){
    }
  }
      delay(100);    //ちょっと待つ

      //データ計測・送信
      dataAquisition();
      Serial.println(data);
      readComplete = false;
}

//Androidからのメッセージを取得
void SerialEvent(){   //オレンジにならないけどコンパイルは通る。
    char inChar = (char)Serial.read();  //1バイトずつ読む
    inputMessage += inChar;
    if(inChar == '\n'){
    }
    readComplete = true;
}

//データ取得
void dataAquisition(){
  count++;
  int times = millis();
  
  data += (String)count;
  data += ",";
  data += (String)times;
}
