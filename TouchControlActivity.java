package fpa.projeto.navegador;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
//import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class TouchControlActivity extends FragmentActivity implements OnTouchListener {	
	
		private ImageView imageView;
		private Bitmap bitmap;
		private Canvas canvas;
		private Paint paint;
		private float downx = 0, downy = 0, upx = 0, upy = 0;
		private boolean touch1 = true;
		private float[] p_x = new float[5];
		private float[] p_y = new float[5];
		private int count = 0;
		private Button btn_percurso;
		
		private static final String TAG = "navegador";
		
		//Para conexão à serial por bluetooth, usa-se o SPP UUID padrão da serial
		private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		private BluetoothDevice device;	
		private OutputStream out;
		private BluetoothSocket socket;
		private boolean abortar = false;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_touch);
			
			//Recupera o dispositico selecionado na lista
			device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			
			imageView = (ImageView) this.findViewById(R.id.imageView1);
			btn_percurso = (Button)findViewById(R.id.btn_rota);
			btn_percurso.setOnClickListener(new ClickStartRota());
			//Display currentDisplay = getWindowManager().getDefaultDisplay();
			DisplayMetrics currentDisplay = getResources().getDisplayMetrics();
			float dw = currentDisplay.widthPixels;
			float dh = currentDisplay.heightPixels;
			bitmap = Bitmap.createBitmap((int) dw, (int) dh, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			paint = new Paint();
			paint.setColor(Color.BLUE);
			paint.setStrokeWidth(5.0f);
			imageView.setImageBitmap(bitmap);
			imageView.setOnTouchListener(this);
		}
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			getMenuInflater().inflate(R.menu.touch_list_menu, menu);
			return true;
		}	
		
		@Override
		public boolean onMenuItemSelected(int featureId, MenuItem item) {
			switch(item.getItemId()) {
				case R.id.menu_abortar:
					Toast.makeText(getBaseContext(), "Abortar Missão", Toast.LENGTH_SHORT).show();
					abortar = true;
					return true;
				case R.id.menu_iniciar:
					btn_percurso.performClick();
					return true;
				case R.id.menu_limpar:
					//Toast.makeText(getBaseContext(), "Limpar Percurso", Toast.LENGTH_SHORT).show();
					limparPercurso();
					return true;
			}
			return false;
		}
		
		private void limparPercurso() {
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			touch1 = true;
			count = 0;
			btn_percurso.setEnabled(false);
			imageView.setEnabled(true);
		}
		
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					if(touch1) {
						downx = event.getX();
						downy = event.getY();
						p_x[count] = downx;
						p_y[count++] = downy;
						canvas.drawCircle(downx, downy, 10.0f, paint);
						imageView.invalidate();
						Toast.makeText(getApplicationContext(), "x: " + downx + ", y: " + downy,
								Toast.LENGTH_SHORT).show();
						touch1 = false;
					} else {
						upx = event.getX();
						upy = event.getY();
						
						p_x[count] = upx;
						p_y[count++] = upy;
						
						if(count == 5) {
							btn_percurso.setEnabled(true);
							imageView.setEnabled(false);
						}
						
						canvas.drawLine(downx, downy, upx, upy, paint);	
						
						downx = upx;
						downy = upy;
						imageView.invalidate();
						Toast.makeText(getApplicationContext(), "x: " + downx + ", y: " + downy,
								Toast.LENGTH_SHORT).show();
					}					
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:					
					/*
					upx = event.getX();
					upy = event.getY();
					canvas.drawLine(downx, downy, upx, upy, paint);
					imageView.invalidate();
					*/
					break;
				case MotionEvent.ACTION_CANCEL:
					break;
				default:
					break;
			}
			return true;
		}
		
		@Override
		protected void onResume() {		
			super.onResume();
			Log.d(TAG, "Tentando estabelecer conexão...");
			
			try {
				socket = device.createRfcommSocketToServiceRecord(uuid);
			} catch(IOException e) {
				Log.e(TAG, "Erro ao conectar: " + e.getMessage(), e);
				errorExit("Erro Fatal", "Falha ao criar socket: " + e.getMessage());
			}
			
			//Estabelecendo conexão
			Log.d(TAG, "Conexão remota");
			try {
				socket.connect();
				Log.d(TAG,"Conexão estabelecida. Link de dados aberto");
			} catch(IOException e) {
				try {
					socket.close();
				} catch(IOException e2) {
					errorExit("Erro Fatal", "Incapaz de fechar socket ao falhar conexão: " + e2.getMessage());
				}
			}
			
			//Criando fluxo de dados para comunicação com o servidor
			Log.d(TAG, "Criando Socket");
			try {
				out = socket.getOutputStream();
			} catch (IOException e) {
				errorExit("Erro Fatal", "Falha ao criar fluxo de saída: " + e.getMessage());
			}
		}
		
		@Override
		protected void onPause() {
			super.onPause();
			if(out != null) {
				try {
					out.flush();
				} catch(IOException e) {
					errorExit("Erro Fatal", "Falha ao tentar limpar fluxo de saída de dados: " + e.getMessage());
				}
			}
			
			if(socket != null) {			
				try {
					socket.close();
				} catch(IOException e2) {
					errorExit("Erro Fatal", "Falha ao fechar socket: " + e2.getMessage());
				}
			}
		}
		
		private void errorExit(String tit, String msn) {
			Toast msg = Toast.makeText(getBaseContext(), tit + " - " + msn, Toast.LENGTH_SHORT);
			msg.show();
			finish();
		}
		
		public void sendData(String message) {
			byte[] mBuffer = message.getBytes();
			
			Log.d(TAG, "Enviando dados: " + message);
			try {
				out.write(mBuffer);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected void onDestroy() {		
			super.onDestroy();
			
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(socket != null) {
					socket.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}		
		
		private class ClickStartRota implements OnClickListener {
			private double anguloRot, distance;
			private int pos_c = 12, pos_p;
			private long time, now;
			@Override
			public void onClick(View v) {
				for(int i = 1; i < p_x.length; i++) {
					if(abortar) {
						abortar = false;
						limparPercurso();
						return;
					}
					if(i == 1)
						pos_c = 12;
					pos_p = getClockValue(p_x[i - 1], p_y[i - 1], p_x[i], p_y[i]);
					anguloRot = getAnguloRot(pos_c, pos_p);
					//Toast.makeText(getApplicationContext(), "Posp: " + pos_p + " Ângulo: " + anguloRot,
							//Toast.LENGTH_LONG).show();
					rotacionarBloco(anguloRot);
					deslocarBloco(distance);
					pos_c = pos_p;
				}
			}
			
			private int getAnguloRot(int c, int p) {
				if(p - c == 0)
					return 0;
				else if((p - c == 1) || (p - c == -11))
					return -30;
				else if((p - c == -1) || (p - c == 11))
					return 30;
				else if((p - c == 2) || (p - c == -10))
					return -60;
				else if((p - c == -2) || (p - c == 10))
					return 60;
				else if((p - c == 3) || (p - c == -9))
					return -90;
				else if((p - c == -3) || (p - c == 9))
					return 90;
				else if((p - c == 4) || (p - c == -8))
					return -120;
				else if((p - c == -4) || (p - c == 8))
					return 120;
				else if((p - c == 5) || (p - c == -7))
					return -150;
				else if((p - c == -5) || (p - c == 7))
					return 150;
				else 
					return 180;
			}
			
			private int getClockValue(float x1, float y1, float x2, float y2) {
				float dx = x2 - x1;
				float dy = y2 - y1;
				distance = Math.sqrt(Math.pow(dy, 2) + Math.pow(dx, 2));
				double angle = getAngle(x1, y1, x2, y2);
				
				if(dy < 0 && dx == 0) return 12;
				if(dy > 0 && dx == 0) return 6;
				
				if((dy <= 0) && (dx > 0)) {
					if(angle >= 60.0 && angle < 90)
						return 1;
					else if(angle >= 30.0 && angle < 60)
						return 2;
					else return 3;
				} else if((dy >= 0) && (dx > 0)) {
					if(angle >= 60.0 && angle < 90)
						return 6;
					else if(angle >= 30.0 && angle < 60)
						return 5;
					else return 4;
				} else if((dy >= 0) && (dx < 0)) {
					if(angle >= 60.0 && angle < 90)
						return 7;
					else if(angle >= 30.0 && angle < 60)
						return 8;
					else return 9;
				} else {
					if(angle >= 60.0 && angle < 90)
						return 12;
					else if(angle >= 30.0 && angle < 60)
						return 11;
					else return 10;
				}
				
			}
			
			private double getAngle(float x1, float y1, float x2, float y2) {
				float dx = x2 - x1;
				float dy = y2 - y1;
				
				if(dx == 0) 
					return 90;
				else 				
					return Math.abs(Math.toDegrees(Math.atan(dy/dx)));
			}
			
			private void rotacionarBloco(double ang) {
				//Rotação anti-horária: ang positivo
				//Rotação horária:		ang negativo				
				//String sent = (ang >= 0 ? "Anti-horaria" : "horária");
				//Toast.makeText(getApplicationContext(), Math.abs(ang) + " " + sent, Toast.LENGTH_SHORT).show();
				
				//Admitindo que  bloco gasta 1.5s para dar uma volta completa (360g/1s = 360g/s)				
				//now = System.currentTimeMillis();
				time = System.currentTimeMillis();
				now = 0;
				while(now <= Math.abs(ang) * 1000.0 / 360.0) {
					
					if(ang > 0) {
						sendData("L");
					}
					if(ang < 0) {
						sendData("R");
					}
					now = System.currentTimeMillis() - time;
				};
				
				sendData("r");				
				
				time = System.currentTimeMillis();
				now = 0;
				while(now < 1000.0) {
					now = System.currentTimeMillis() - time;
				}
			}
			
			private void deslocarBloco(double dist) {
				//Toast.makeText(getApplicationContext(), "Deslocando " + distance + " und", Toast.LENGTH_SHORT).show();
				//Admitindo um deslocamento do bloco de 100und/s							
				time = System.currentTimeMillis();
				now = 0;
				while(now < distance * 10.0) {
					sendData("U");	
					now = System.currentTimeMillis() - time;
				}
				
				sendData("u");
				
				time = System.currentTimeMillis();
				now = 0;
				while(now < 1000.0) {
					now = System.currentTimeMillis() - time;
				}
			}
			
			
		}
		

}





















