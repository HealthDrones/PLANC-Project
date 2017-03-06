/******************************************
*  Projeto FPA UFRPE                      *
*  Alberto Rogério e Silva                *
******************************************/

//Biblioteca para driver de controle dos motores
#include <AFMotor.h>

#define  sensor_frente  2  //Sensor óptico-reflexivo posicionado a frente do módulo (frontal)
#define  sensor_tras  3    //Sensor óptico-reflexivo posicionado atrás do módulo (traseiro)  

AF_DCMotor motor_esq(1);   //Motor CC esquerdo
AF_DCMotor motor_dir(2);   //Motor CC direito

//Constantes para controle dos motores
/*******************
1 - FRENTE
2 - TRÁS
3 - PARA
*******************/

int x = 3, y = 3, velozX = 100, velozY = 120, valorf, valort; 
double vf = 0.0, vt = 0.0;
char dataFromBT;
unsigned long tempo, diferent = 0L;

//Função para setar velocidade e orientação dos motores
void setVelocidade(int vx, int vy, int ox, int oy);

//Função para contornar obstáculo detectado pelo sensor frontal
void contornarObstaculo();

void setup() {
  Serial.begin(9600); 
  motor_esq.setSpeed(100);
  motor_dir.setSpeed(120);
  
  motor_esq.run(RELEASE);
  motor_dir.run(RELEASE); 
 
  //Calibrando sensores por meio de 10 leituras iniciais
  //para usar os valores médios
  for(int i = 0; i < 10; i++) {
     vf = vf + analogRead(sensor_frente);
     vt = vt + analogRead(sensor_tras);
  } 
  //Calculando média
  vf = vf / 10.0;
  vt = vt / 10.0;
}

void loop() {
  valorf = analogRead(sensor_frente);
  valort = analogRead(sensor_tras);
  diferent = 0L;
  
  if(valorf < (vf - 50)) { //Sensor frontal detectou obstáculo
      Serial.print("Obstáculo encontrado! Voltando.");
      setVelocidade(0, 0, 3, 3);      
      delay(10);
      contornarObstaculo();       
    } else  if (valort < (vt - 50)) { //Sensor traseiro detectou obstáculo
      Serial.print("Obstáculo encontrado! Adiantando.");
      setVelocidade(0, 0, 3, 3);
      delay(10);   
      tempo = millis();
      while(diferent < 1000L) { //Avança durante 1 segundo
        setVelocidade(100, 120, 1, 1);
        diferent = millis() - tempo;
      }
      setVelocidade(0, 0, 3, 3);      
      delay(10); 
    }   
  
    //Dados via Bluetooth
    if (Serial.available()) {
      dataFromBT = Serial.read();   
        switch(dataFromBT) {
          case 'U': x = 1; y = 1; velozX = 100; velozY = 120; break;      
          case 'L': x = 3; y = 1; velozX = 0; velozY = 120; break;       
          case 'R': x = 1; y = 3; velozX = 100; velozY = 0; break;       
          case 'D': x = 2; y = 2; velozX = 100; velozY = 120; break;
          case 'r':
          case 'l':
          case 'u':  
          case 'd': x = 3; y = 3; velozX = 0; velozY = 0; break;
          default:  x = 3; y = 3;
        }
        setVelocidade(velozX, velozY, x, y);
    }      
}

void contornarObstaculo() {
  diferent = 0L;
  tempo = millis();
  while(diferent < 1000L) {  //Retrocede durante 1 segundo
    setVelocidade(100, 120, 2, 2);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  /*
  diferent = 0L;
  tempo = millis();
  while(diferent < 800L) {  //Vira à direita por 800 ms
    setVelocidade(100, 0, 1, 3);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  
  diferent = 0L;
  tempo = millis();
  while(diferent < 2000L) {  //Segue para frente por 2 segundos
    setVelocidade(100, 120, 1, 1);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  
  diferent = 0L;
  tempo = millis();
  while(diferent < 800L) {  //Vira à esquerda por 800 ms
    setVelocidade(0, 120, 3, 1);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  
  diferent = 0L;
  tempo = millis();
  while(diferent < 2000L) {  //Segue para frente por 2 segundos
    setVelocidade(100, 120, 1, 1);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  
  diferent = 0L;
  tempo = millis();
  while(diferent < 800L) {  //Vira à esquerda por 800 ms
    setVelocidade(0, 120, 3, 1);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  
  diferent = 0L;
  tempo = millis();
  while(diferent < 2000L) {  //Segue para frente por 2 segundos
    setVelocidade(100, 120, 1, 1);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  
  diferent = 0L;
  tempo = millis();
  while(diferent < 800L) {  //Vira à direita por 800 ms
    setVelocidade(100, 0, 1, 3);
    diferent = millis() - tempo;
  }
  setVelocidade(0, 0, 3, 3);  //Para     
  delay(10);
  diferent = 0L;
  */
}

void setVelocidade(int vx, int vy, int ox, int oy) {
  motor_esq.setSpeed(vx);
  motor_dir.setSpeed(vy);
  motor_esq.run(ox);
  motor_dir.run(oy);
}

