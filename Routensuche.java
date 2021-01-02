package de.fpprogs.routensuche;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

public class Routensuche
{
	private JFrame routensuche;

	static HashMap<String, HashMap<String, Tuple<Float, Float>>> db = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				Routensuche window = new Routensuche();
				try
				{
					window.routensuche.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(window.routensuche, e.getMessage(), "Fehler!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Routensuche()
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		final JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return ".csv";
			}
			
			@Override
			public boolean accept(File f)
			{
				if (f.isDirectory()) return true;
				
				String ext = null;
				String s = f.getName();
				int i = s.lastIndexOf('.');

				if (i > 0 &&  i < s.length() - 1)
				{
					ext = s.substring(i+1).toLowerCase();
				}
				
				return "csv".equals(ext);
			}
		});

		routensuche = new JFrame();
		routensuche.setTitle("Routensuche");
		routensuche.setBounds(100, 100, 469, 213);
		routensuche.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		routensuche.getContentPane().setLayout(new MigLayout("", "10[][grow][]10", "10[][][][][][]10"));

		JLabel lblTeilstrecken = new JLabel("Teilstrecken:");
		routensuche.getContentPane().add(lblTeilstrecken, "cell 0 0,alignx trailing,growy");

		JTextField txt_teilstrecken = new JTextField();
		txt_teilstrecken.setEditable(false);
		routensuche.getContentPane().add(txt_teilstrecken, "cell 1 0,grow");
		txt_teilstrecken.setColumns(10);

		JButton btnDateiffnen = new JButton("Datei öffnen");
		btnDateiffnen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int returnVal = fc.showOpenDialog(routensuche);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					txt_teilstrecken.setText(fc.getSelectedFile().getPath());
				}
			}
		});
		URL icoOpen = Routensuche.class.getResource("/javax/swing/plaf/metal/icons/ocean/file.gif");
		if (icoOpen != null)
			btnDateiffnen.setIcon(new ImageIcon(icoOpen));
		routensuche.getContentPane().add(btnDateiffnen, "cell 2 0,growx");

		JLabel lblRelationen = new JLabel("Relationen:");
		routensuche.getContentPane().add(lblRelationen, "cell 0 1,alignx trailing,growy");

		JTextField txt_relationen = new JTextField();
		txt_relationen.setEditable(false);
		routensuche.getContentPane().add(txt_relationen, "cell 1 1,grow");
		txt_relationen.setColumns(10);

		JButton btnDateiffnen_1 = new JButton("Datei öffnen");
		btnDateiffnen_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int returnVal = fc.showOpenDialog(routensuche);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					txt_relationen.setText(fc.getSelectedFile().getPath());
				}
			}
		});
		if (icoOpen != null)
			btnDateiffnen_1.setIcon(new ImageIcon(icoOpen));
		routensuche.getContentPane().add(btnDateiffnen_1, "cell 2 1,growx");

		JSeparator separator = new JSeparator();
		routensuche.getContentPane().add(separator, "cell 0 2 3 1");

		JLabel lblRouten = new JLabel("Routen:");
		routensuche.getContentPane().add(lblRouten, "cell 0 3,alignx trailing,growy");

		JTextField txt_routen = new JTextField();
		txt_routen.setEditable(false);
		routensuche.getContentPane().add(txt_routen, "cell 1 3,grow");
		txt_routen.setColumns(10);

		JButton btnOrdnerffnen = new JButton("Ordner öffnen");
		btnOrdnerffnen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fc.setSelectedFile(new File("Routen.csv"));
				int returnVal = fc.showSaveDialog(routensuche);

				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					txt_routen.setText(fc.getSelectedFile().getPath());
				}
			}
		});
		URL icoDir = Routensuche.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif");
		if (icoDir != null)
			btnOrdnerffnen.setIcon(new ImageIcon(icoDir));
		routensuche.getContentPane().add(btnOrdnerffnen, "cell 2 3,growx");

		JSeparator separator_1 = new JSeparator();
		routensuche.getContentPane().add(separator_1, "cell 0 4 3 1");

		JButton btnBerechnen = new JButton("Berechnen");
		btnBerechnen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					findRoutes(txt_relationen.getText(), txt_teilstrecken.getText(), txt_routen.getText());
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
					JOptionPane.showMessageDialog(routensuche, e1.getMessage(), "Fehler!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		URL icoComp = Routensuche.class.getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif");
		if (icoComp != null)
			btnBerechnen.setIcon(new ImageIcon(icoComp));
		routensuche.getContentPane().add(btnBerechnen, "cell 2 5,growx");
	}

	public static float getRouteLength(List<String> route, boolean tag)
	{
		float cntr = 0;
		String first = null;

		if (route == null) return 0;

		for (String st : route)
		{
			if (first == null)
			{
				first = st;
				continue;
			}
			if (st == null) return 0;

			cntr += tag ? db.get(first).get(st).y : db.get(first).get(st).x;
			first = st;
		}

		return cntr;
	}

	public static List<String> calculateRoute(String start, String target, boolean tag)
	{
		if (start == null || target == null) return null;

		// DIJKSTRA
		HashSet<String> visited = new HashSet<>();
		PriorityQueue<Tuple<String, Float>> queue = new PriorityQueue<>(10, Comparator.comparingDouble(Tuple::getY));
		HashMap<String, String> pre = new HashMap<>();

		pre.put(start, start);
		queue.add(new Tuple<>(start, 0f));
		visited.add(start);

		while (!queue.isEmpty())
		{
			Tuple<String, Float> dequeued = queue.poll();
			String p = dequeued.x;
			Float d = dequeued.y;

			if (db.get(p) == null) return null;

			Iterator<Entry<String, Tuple<Float, Float>>> it = db.get(p).entrySet().iterator();
			while (it.hasNext())
			{
				Entry<String, Tuple<Float, Float>> entry = it.next();
				String nb = entry.getKey();

				float d_new = d + (tag ? entry.getValue().y : entry.getValue().x);
				if (!visited.contains(nb))
				{
					pre.put(nb, p);

					queue.offer(new Tuple<>(nb, d_new));
					visited.add(nb);
				}
				else
				{
					Iterator<Tuple<String, Float>> q_it = queue.iterator();
					while (q_it.hasNext())
					{
						Tuple<String, Float> q_entry = q_it.next();
						if (q_entry.x.equals(nb))
						{
							// Shorter --> update
							if (d_new < q_entry.y)
							{
								q_it.remove();

								queue.offer(new Tuple<>(nb, d_new));
								pre.put(nb, p);
							}

							break;
						}
					}
				}
			}
		}

		List<String> route = new LinkedList<>();
		String p = target;
		while (p != null && !p.equals(start))
		{
			route.add(0, p);
			p = pre.get(p);
		}

		route.add(0, p);

		return route;
	}

	public void findRoutes(String relationen, String teilstrecken, String ausgabe) throws IOException
	{
		String line = "";
		String cvsSplitBy = ";";
		
		db = new HashMap<>();

		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ausgabe), "ISO-8859-15"));
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(teilstrecken), "ISO-8859-15"));
		while ((line = br.readLine()) != null)
		{
			String[] route = line.split(cvsSplitBy);

//			System.out.println("Route [von=" + route[0] + " , nach=" + route[1] + " , Strecke=" + route[2] + " , Fz NJ=" + route[3] + " , Fz Tag=" + route[4] + "]");

			if ("Fz NJ".equals(route[3])) continue;

			if (route[0].startsWith("0") || route[1].startsWith("0")) continue;

			Float fz_nj = Float.parseFloat(route[3].replace(',', '.'));
			Float fz_tag = Float.parseFloat(route[4].replace(',', '.'));

			if (fz_nj == 0 || fz_tag == 0) continue;

			// Hin
			HashMap<String, Tuple<Float, Float>> entry = db.get(route[0]);
			if (entry == null)
			{
				entry = new HashMap<>();
				db.put(route[0], entry);
			}

			entry.put(route[1], new Tuple<>(fz_nj, fz_tag));

			// Rueck
			HashMap<String, Tuple<Float, Float>> entry2 = db.get(route[1]);
			if (entry2 == null)
			{
				entry2 = new HashMap<>();
				db.put(route[1], entry2);
			}

			entry2.put(route[0], new Tuple<>(fz_nj, fz_tag));
		}
		br.close();

		wr.write("relation;von;nach;route tag;fz tag;route nj;fz nj");
		wr.newLine();

		br = new BufferedReader(new InputStreamReader(new FileInputStream(relationen), "ISO-8859-15"));
		while ((line = br.readLine()) != null)
		{
			String start;
			String end;

			try
			{
				start = line.substring(0, 3);
				end = line.substring(3);
			}
			catch (Exception e)
			{
				continue;
			}

			if (start.startsWith("0") || end.equals("0")) continue;

			List<String> tag = calculateRoute(start, end, true);
			List<String> nacht = calculateRoute(start, end, false);

			float length_tag = getRouteLength(tag, true);
			float length_nacht = getRouteLength(nacht, false);

			if (length_tag == 0 || length_nacht == 0) continue;

			wr.write(line + ";" + start + ";" + end + ";" + String.valueOf(tag) + ";"
					+ String.valueOf(length_tag).replace('.', ',') + ";" + String.valueOf(nacht) + ";"
					+ String.valueOf(length_nacht).replace('.', ','));
			wr.newLine();
		}
		br.close();
		wr.close();
		
		JOptionPane.showMessageDialog(routensuche, "Routendatei erfolgreich geschrieben!", "Beendet", JOptionPane.INFORMATION_MESSAGE);
	}
}

class Tuple<X, Y>
{
	public final X x;
	public final Y y;

	public Tuple(X x, Y y)
	{
		this.x = x;
		this.y = y;
	}

	public X getX()
	{
		return x;
	}

	public Y getY()
	{
		return y;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Tuple other = (Tuple) obj;
		if (x == null)
		{
			if (other.x != null)
				return false;
		}
		else if (!x.equals(other.x))
			return false;
		if (y == null)
		{
			if (other.y != null)
				return false;
		}
		else if (!y.equals(other.y))
			return false;
		return true;
	}
}
