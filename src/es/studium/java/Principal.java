package es.studium.java;

import javax.swing.JFrame;

public class Principal
{
	public static void main(String[] args)
	{
		JFrame ventana = new JFrame("El Pez Glotón");
		PanelJuego panel = new PanelJuego();
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ventana.setContentPane(panel);
		ventana.pack();
		ventana.setResizable(false);
		ventana.setLocationRelativeTo(null);
		ventana.setVisible(true);
		panel.requestFocusInWindow();
		Sonidos.iniciarMusica();
	}
}