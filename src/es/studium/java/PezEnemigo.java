package es.studium.java;

public class PezEnemigo extends Thread
{
	private PanelJuego panel;
	private int tamano;
	private int direccion; 
	private double velocidad;
	private double x;
	private double y;
	private volatile boolean activo = true;
	private boolean salioDePantalla = false;

	public PezEnemigo(PanelJuego panel, int tamano, double y, boolean desdeIzquierda)
	{
		this.panel = panel;
		this.tamano = tamano;
		this.y = y;
		this.velocidad = 1 + tamano * 0.2;
		
		if (desdeIzquierda)
		{
			this.direccion = 1;
			this.x = -PanelJuego.diametro(tamano) * 2;
		}
		else
		{
			this.direccion = -1;
			this.x = PanelJuego.ANCHO + PanelJuego.diametro(tamano) * 2;
		}
		setDaemon(true);
	}

	public double getX()
	{
		return x;
	}

	public double getY()

	{
		return y;
	}

	public int getTamano()
	{
		return tamano;
	}

	public int getDireccion()
	{
		return direccion;
	}

	public void detener()
	{
		activo = false;
	}

	public void run()
	{
		double limite = PanelJuego.diametro(tamano) * 2 + 10;
		
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

			x = x + direccion * velocidad;

			if (x < -limite || x > PanelJuego.ANCHO + limite)
			{
				salioDePantalla = true;
				activo = false;
			}
		}
		if (salioDePantalla)
		{
			panel.eliminarPez(this);
		}

	}

}