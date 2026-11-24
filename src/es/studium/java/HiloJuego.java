package es.studium.java;

public class HiloJuego extends Thread
{
	private PanelJuego panel;
	private volatile boolean activo = true;

	public HiloJuego(PanelJuego panel)
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
				Thread.sleep(16);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if (activo)
			{
				panel.actualizar();

			}

		}

	}

}