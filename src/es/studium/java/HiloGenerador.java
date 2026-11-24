package es.studium.java;

import java.util.Random;

public class HiloGenerador extends Thread
{
	private PanelJuego panel;
	private Random azar = new Random();
	private volatile boolean activo = true;

	public HiloGenerador(PanelJuego panel)
	{
		this.panel = panel;
		setDaemon(true);
	}
	public void detener()
	{
		activo = false;
	}
	public void run()
	{
		while (activo)
		{
			try
			{
				Thread.sleep(500 + azar.nextInt(900));
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if (activo)
			{
				panel.generar();

			}

		}

	}

}