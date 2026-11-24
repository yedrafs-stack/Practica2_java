package es.studium.java;

public class Jugador
{
	private double x;
	private double y;
	private int tamano;
	private boolean mirandoDerecha = true;

	public Jugador()
	{
		reiniciar();
	}

	public synchronized void reiniciar()
	{
		x = PanelJuego.ANCHO / 2;
		y = PanelJuego.ALTO / 2;
		tamano = PanelJuego.TAM_INICIAL;
		mirandoDerecha = true;
	}

	public synchronized double getX()
	{
		return x;
	}
	public synchronized double getY()
	{
		return y;
	}
	public synchronized int getTamano()
	{
		return tamano;
	}
	public synchronized boolean isMirandoDerecha()
	{
		return mirandoDerecha;
	}
	public synchronized void crecer(int cantidad)
	{
		tamano = tamano + cantidad;
	}
	public synchronized double velocidad()
	{
		return 3 + tamano * 0.1;
	}
	public synchronized void mover(double dx, double dy)
	{
		x = x + dx;
		y = y + dy;
		if (x < 0)
		{
			x = 0;
		}
		if (x > PanelJuego.ANCHO)
		{
			x = PanelJuego.ANCHO;
		}
		if (y < PanelJuego.ZONA_SUPERIOR)
		{
			y = PanelJuego.ZONA_SUPERIOR;
		}

		if (y > PanelJuego.ALTO - PanelJuego.ARENA)

		{
			y = PanelJuego.ALTO - PanelJuego.ARENA;
		}
		if (dx > 0)
		{
			mirandoDerecha = true;
		}
		if (dx < 0)
		{
			mirandoDerecha = false;

		}

	}

}