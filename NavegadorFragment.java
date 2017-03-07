package fpa.projeto.navegador;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.Button;
import android.widget.ImageButton;

public class NavegadorFragment extends Fragment {
	
	private ImageButton btn_sair;
	private ImageButton btn_listar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_navegador, container, false);
		
		btn_sair = (ImageButton)v.findViewById(R.id.btn_sair);
		btn_sair.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getActivity().finish();				
			}
		});
		
		btn_listar = (ImageButton)v.findViewById(R.id.btn_listar);
		btn_listar.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(), DispositivosBT.class);
				startActivity(intent);
			}
		});
		
		return v;
	}

}
