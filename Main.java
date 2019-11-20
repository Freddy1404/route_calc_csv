package de.fpprogs.routensuche;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class Main 
{
	static HashMap<String, HashMap<String, Tuple<Float, Float>>> db = null;
	
	public static void main(String[] args) 
	{
		String line = "";
        String cvsSplitBy = ";";
        
        db = new  HashMap<String, HashMap<String, Tuple<Float, Float>>>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "ISO-8859-15"))) 
        {
            while ((line = br.readLine()) != null) 
            {
                String[] route = line.split(cvsSplitBy);

//              System.out.println("Route [von=" + route[0] + " , nach=" + route[1] + " , Strecke=" + route[2] + " , Fz NJ=" + route[3] + " , Fz Tag=" + route[4] + "]");
                
                if ("Fz NJ".equals(route[3])) continue;
                
                Float fz_nj = Float.parseFloat(route[3].replace(',','.'));
                Float fz_tag = Float.parseFloat(route[4].replace(',','.'));
                
                if (fz_nj == 0 || fz_tag == 0) continue;
                
                // Hin
                HashMap<String, Tuple<Float, Float>> entry = db.get(route[0]);
                if (entry == null)
                {
                	entry = new HashMap<String, Tuple<Float, Float>>();
                	db.put(route[0], entry);
                }
                
                entry.put(route[1], new Tuple<Float, Float>(fz_nj, fz_tag));
                
                // Rueck
                HashMap<String, Tuple<Float, Float>> entry2 = db.get(route[1]);
                if (entry2 == null)
                {
                	entry2 = new HashMap<String, Tuple<Float, Float>>();
                	db.put(route[1], entry2);
                }
                
                entry2.put(route[0], new Tuple<Float, Float>(fz_nj, fz_tag));
            }
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
//      String start1 = "Wie";
//    	String ende1 = "Kob";
//        
//    	System.out.println(calculateRoute(start1, ende1));
    	
        System.out.println("relation;von;nach;route tag;fz tag;route nj;fz nj");
        
    	try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]), "ISO-8859-15"))) 
        {
            while ((line = br.readLine()) != null) 
            {
            	String start;
            	String end;
            	
				try {
					start = line.substring(0, 3);
					end = line.substring(3);
				} catch (Exception e) {
					continue;
				}
            	 
            	if (start.startsWith("0") || end.equals("0")) continue;
            	
            	List<String> tag = calculateRoute(start, end, true);
            	List<String> nacht = calculateRoute(start, end, false);
            	
            	float length_tag = getRouteLength(tag, true);
            	float length_nacht = getRouteLength(nacht, false);
            	
            	if (length_tag == 0 || length_nacht == 0) continue;
            	
            	System.out.println(line + ";" + start + ";" + end + ";" + String.valueOf(tag) + ";" + String.valueOf(length_tag).replace('.', ',') + ";" + String.valueOf(nacht) + ";" + String.valueOf(length_nacht).replace('.', ','));
            }
        }
    	catch (IOException e) 
        {
            e.printStackTrace();
        }
	}
	
	public static float getRouteLength(List<String> route, boolean tag)
	{
		float cntr = 0;
		String first = null;
		
		if (route == null) return 0;
		
		for(String st : route)
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
		
		// BREADTH-FIRST
		HashSet<String> visited = new HashSet<String>();
		PriorityQueue<Tuple<String, Float>> queue = new PriorityQueue<Tuple<String, Float>>(10, Comparator.comparingDouble(Tuple::getY));
		HashMap<String, String> pre = new HashMap<String, String>();
		
		pre.put(start, start);		
		queue.add(new Tuple<String, Float>(start, 0f));
		visited.add(start);
		
		while(!queue.isEmpty())
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
					
					queue.offer(new Tuple<String, Float>(nb, d_new));
					visited.add(nb);
				}
				else
				{
					Iterator<Tuple<String, Float>> q_it = queue.iterator();
					while(q_it.hasNext())
					{
						Tuple<String, Float> q_entry = q_it.next();
						if (q_entry.x.equals(nb))
						{
							// Shorter --> update
							if (d_new < q_entry.y)
							{
								q_it.remove();
								
								queue.offer(new Tuple<String, Float>(nb, d_new));
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

	public X getX() {
		return x;
	}

	public Y getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Tuple other = (Tuple) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}
}