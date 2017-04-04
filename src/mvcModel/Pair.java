package mvcModel;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.rdf.model.Resource;

public class Pair 
{
	public HashMap<Resource, ArrayList<Resource>> pairs;

	public Pair()
	{
		pairs = new HashMap<>();
	}


	public Boolean checkIfPairExists(Resource res1, Resource res2)
	{
		if(this.pairs.containsKey(res1))
		{
			if(this.pairs.get(res1).contains(res2))
			{
				return true;
			}
		}
		if(this.pairs.containsKey(res2))
		{
			if(this.pairs.get(res2).contains(res1))
			{
				return true;
			}
		}
		return false;
	}


	public void addPair(Resource res1, Resource res2)
	{
		if(this.checkIfPairExists(res1, res2)== false)
		{
			if(this.pairs.containsKey(res1))
			{
				this.pairs.get(res1).add(res2);
			}
			else
			{
				ArrayList<Resource> list = new ArrayList<>();
				list.add(res2);
				pairs.put(res1, list);
			}
		}
		
	}

}
