package es.studium.java;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;

public class PanelJuego extends JPanel implements KeyListener, MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;

	public static final int ANCHO = 900;
	public static final int ALTO = 600;
	public static final int ZONA_SUPERIOR = 40;
	public static final int ARENA = 40;
	public static final int TAM_INICIAL = 5;
	public static final int TAM_FINAL = 50;

	private static final int BIENVENIDA = 0;
	private static final int JUGANDO = 1;
	private static final int DERROTA = 2;
	private static final int VICTORIA = 3;

	private volatile int estado = BIENVENIDA;
	private volatile boolean arriba, abajo, izquierda, derecha;
	private volatile boolean usarRaton = false;
	private volatile int ratonX, ratonY;

	private Jugador jugador = new Jugador();
	private ArrayList<PezEnemigo> peces = new ArrayList<PezEnemigo>();
	private ArrayList<Comida> comidas = new ArrayList<Comida>();
	private Random azar = new Random();
	private HiloJuego hiloJuego;
	private HiloGenerador hiloGenerador;

	public PanelJuego()
	{
		setPreferredSize(new Dimension(ANCHO, ALTO));
		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public static double diametro(int tamano)
	{
		return 10 + tamano * 2.4;
	}

	public static double radio(int tamano)
	{
		return diametro(tamano) * 0.55;
	}

	private double distancia(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	private void iniciarPartida()
	{
		detenerHilos();
		synchronized (peces)
		{
			peces.clear();
		}
		synchronized (comidas)
		{
			comidas.clear();
		}
		jugador.reiniciar();
		for (int i = 0; i < 5; i++)
		{
			crearComida();
		}
		estado = JUGANDO;
		hiloJuego = new HiloJuego(this);
		hiloGenerador = new HiloGenerador(this);
		hiloJuego.start();
		hiloGenerador.start();
	}

	private void detenerHilos()
	{
		if (hiloJuego != null)
		{
			hiloJuego.detener();
		}
		if (hiloGenerador != null)
		{
			hiloGenerador.detener();
		}
		synchronized (peces)
		{
			for (int i = 0; i < peces.size(); i++)
			{
				peces.get(i).detener();
			}
		}
	}

	private void finalizar(int nuevoEstado)
	{
		estado = nuevoEstado;
		detenerHilos();
		if (nuevoEstado == VICTORIA)
		{
			Sonidos.efecto("victoria.wav");
		}
		else
		{
			Sonidos.efecto("comido.wav");
		}
		repaint();
	}

	public void actualizar()
	{
		if (estado != JUGANDO)
		{
			return;
		}
		moverJugador();
		comprobarColisiones();
		repaint();
	}

	private void moverJugador()
	{
		double v = jugador.velocidad();
		double dx = 0;
		double dy = 0;
		if (usarRaton)
		{
			double d = distancia(ratonX, ratonY, jugador.getX(), jugador.getY());
			if (d > v)
			{
				dx = (ratonX - jugador.getX()) / d * v;
				dy = (ratonY - jugador.getY()) / d * v;
			}
		}
		else
		{
			if (izquierda)
				dx = -v;
			if (derecha)
				dx = v;
			if (arriba)
				dy = -v;
			if (abajo)
				dy = v;
		}
		jugador.mover(dx, dy);
	}

	private void comprobarColisiones()
	{
		synchronized (comidas)
		{
			for (int i = comidas.size() - 1; i >= 0; i--)
			{
				Comida c = comidas.get(i);
				if (distancia(c.getX(), c.getY(), jugador.getX(), jugador.getY()) < radio(jugador.getTamano()) + Comida.RADIO)
				{
					comidas.remove(i);
					jugador.crecer(Comida.VALOR);
					Sonidos.efecto("comer.wav");
				}
			}
		}
		synchronized (peces)
		{
			for (int i = peces.size() - 1; i >= 0; i--)
			{
				PezEnemigo p = peces.get(i);
				double contacto = (radio(jugador.getTamano()) + radio(p.getTamano())) * 0.75;
				if (distancia(p.getX(), p.getY(), jugador.getX(), jugador.getY()) < contacto)
				{
					if (p.getTamano() < jugador.getTamano())
					{
						p.detener();
						peces.remove(i);
						jugador.crecer(p.getTamano());
						Sonidos.efecto("comer.wav");
					}
					else
					{
						finalizar(DERROTA);
						return;
					}
				}
			}
		}
		if (jugador.getTamano() >= TAM_FINAL)
		{
			finalizar(VICTORIA);
		}
	}

	public void generar()
	{
		if (estado != JUGANDO)
		{
			return;
		}
		if (peces.size() < 9)
		{
			crearPez();
		}
		if (azar.nextBoolean() && comidas.size() < 8)
		{
			crearComida();
		}
	}

	private void crearPez()
	{
		int tamano = 1 + azar.nextInt(jugador.getTamano() + 3);
		double y = ZONA_SUPERIOR + azar.nextInt(ALTO - ZONA_SUPERIOR - ARENA);
		PezEnemigo pez = new PezEnemigo(this, tamano, y, azar.nextBoolean());
		synchronized (peces)
		{
			peces.add(pez);
		}
		pez.start();
	}

	private void crearComida()
	{
		double x = 30 + azar.nextInt(ANCHO - 60);
		double y = ZONA_SUPERIOR + 20 + azar.nextInt(ALTO - ZONA_SUPERIOR - ARENA - 40);
		synchronized (comidas)
		{
			comidas.add(new Comida(x, y));
		}
	}

	public void eliminarPez(PezEnemigo pez)
	{
		synchronized (peces)
		{
			peces.remove(pez);
		}
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(new Color(20, 100, 150));
		g.fillRect(0, 0, ANCHO, ALTO);
		g.setColor(new Color(217, 164, 65));
		g.fillRect(0, ALTO - ARENA, ANCHO, ARENA);

		int miTamano = jugador.getTamano();

		g.setColor(Color.WHITE);
		synchronized (comidas)
		{
			for (int i = 0; i < comidas.size(); i++)
			{
				Comida c = comidas.get(i);
				g.fillOval((int) c.getX() - Comida.RADIO, (int) c.getY() - Comida.RADIO, Comida.RADIO * 2, Comida.RADIO * 2);
			}
		}
		synchronized (peces)
		{
			for (int i = 0; i < peces.size(); i++)
			{
				PezEnemigo p = peces.get(i);
				Color color = new Color(225, 70, 90);
				if (p.getTamano() < miTamano)
				{
					color = new Color(70, 200, 230);
				}
				dibujarPez(g, p.getX(), p.getY(), p.getTamano(), p.getDireccion() > 0, color);
			}
		}
		if (estado != BIENVENIDA)
		{
			dibujarPez(g, jugador.getX(), jugador.getY(), miTamano, jugador.isMirandoDerecha(), new Color(255, 165, 30));
		}
		g.setColor(Color.WHITE);
		g.setFont(new Font("SansSerif", Font.BOLD, 18));
		g.drawString("Tamaño: " + miTamano + " / " + TAM_FINAL, 15, 26);

		if (estado == BIENVENIDA)
		{
			dibujarBienvenida(g);
		}
		else if (estado == DERROTA)
		{
			dibujarFin(g, "¡TE HAN COMIDO!");
		}
		else if (estado == VICTORIA)
		{
			dibujarFin(g, "¡HAS GANADO!");
		}
	}

	private void dibujarPez(Graphics g, double cx, double cy, int tamano, boolean haciaDerecha, Color color)
	{
		int d = (int) diametro(tamano); 
		int w = d * 14 / 10; 
		int x = (int) cx;
		int y = (int) cy;
		int lado = 1; 
		if (haciaDerecha)
		{
			lado = -1;
		}
		int baseCola = x + lado * w / 2;
		int[] colaX = { baseCola, baseCola + lado * d / 2, baseCola + lado * d / 2 };
		int[] colaY = { y, y - d / 3, y + d / 3 };
		g.setColor(color.darker());
		g.fillPolygon(colaX, colaY, 3);
		g.setColor(color);
		g.fillOval(x - w / 2, y - d / 2, w, d);
		g.setColor(Color.BLACK);
		g.fillOval(x - lado * w / 4, y - d / 5, 5, 5);
		g.setColor(Color.WHITE);
		g.setFont(new Font("SansSerif", Font.BOLD, 14));
		g.drawString(String.valueOf(tamano), x - 6, y - d / 2 - 4);
	}

	private void dibujarBienvenida(Graphics g)
	{
		g.setColor(new Color(0, 0, 0, 170));
		g.fillRect(0, 0, ANCHO, ALTO);
		g.setColor(new Color(255, 165, 30));
		g.setFont(new Font("SansSerif", Font.BOLD, 48));
		g.drawString("EL PEZ GLOTÓN", 250, 110);
		g.setColor(Color.WHITE);
		g.setFont(new Font("SansSerif", Font.PLAIN, 20));
		g.drawString("Eres el pez naranja y empiezas con tamaño " + TAM_INICIAL + ".", 150, 190);
		g.drawString("Come burbujas (valen 1) y peces MÁS PEQUEÑOS que tú para crecer.", 150, 230);
		g.drawString("Si chocas con un pez IGUAL o MAYOR, te comen y pierdes.", 150, 270);
		g.drawString("Peces AZULES: se pueden comer.   Peces ROJOS: ¡huye!", 150, 310);
		g.drawString("Ganas al llegar al tamaño " + TAM_FINAL + ". Cuanto más grande, más rápido nada.", 150, 350);
		g.drawString("Muévete con los cursores o con el ratón.", 150, 390);
		g.setColor(new Color(130, 255, 150));
		g.setFont(new Font("SansSerif", Font.BOLD, 24));
		g.drawString("Pulsa ENTER o haz clic para empezar", 220, 470);
	}

	private void dibujarFin(Graphics g, String titulo)
	{
		g.setColor(new Color(0, 0, 0, 150));
		g.fillRect(0, 0, ANCHO, ALTO);
		g.setColor(new Color(255, 220, 120));
		g.setFont(new Font("SansSerif", Font.BOLD, 50));
		g.drawString(titulo, 240, ALTO / 2 - 20);
		g.setFont(new Font("SansSerif", Font.BOLD, 22));
		g.drawString("Pulsa ENTER o haz clic para jugar otra vez", 220, ALTO / 2 + 40);
	}

	public void keyPressed(KeyEvent e)
	{
		int k = e.getKeyCode();
		if (k == KeyEvent.VK_ENTER && estado != JUGANDO)
		{
			iniciarPartida();
		}
		if (k == KeyEvent.VK_UP)
		{
			arriba = true;
			usarRaton = false;
		}
		if (k == KeyEvent.VK_DOWN)
		{
			abajo = true;
			usarRaton = false;
		}
		if (k == KeyEvent.VK_LEFT)
		{
			izquierda = true;
			usarRaton = false;
		}
		if (k == KeyEvent.VK_RIGHT)
		{
			derecha = true;
			usarRaton = false;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		int k = e.getKeyCode();
		if (k == KeyEvent.VK_UP)
			arriba = false;
		if (k == KeyEvent.VK_DOWN)
			abajo = false;
		if (k == KeyEvent.VK_LEFT)
			izquierda = false;
		if (k == KeyEvent.VK_RIGHT)
			derecha = false;
	}

	public void keyTyped(KeyEvent e)
	{
	}

	public void mouseMoved(MouseEvent e)
	{
		ratonX = e.getX();
		ratonY = e.getY();
		usarRaton = true;
	}

	public void mouseDragged(MouseEvent e)
	{
		mouseMoved(e);
	}

	public void mousePressed(MouseEvent e)
	{
		requestFocusInWindow();
		if (estado != JUGANDO)
		{
			iniciarPartida();
		}
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}
}