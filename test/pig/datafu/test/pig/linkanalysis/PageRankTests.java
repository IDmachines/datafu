package datafu.test.pig.linkanalysis;


import static org.testng.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.PigTest;
import org.testng.annotations.Test;


import datafu.test.linkanalysis.PageRankTest;
import datafu.test.pig.PigTests;

public class PageRankTests extends PigTests
{
  @Test
  public void pigPageRankTest() throws Exception
  {
    PigTest test = createPigTest("test/pig/datafu/test/pig/linkanalysis/pageRankTest.pig");

    String[] edges = PageRankTest.getWikiExampleEdges();

    Map<String,Integer> nodeIds = new HashMap<String,Integer>();
    Map<Integer,String> nodeIdsReversed = new HashMap<Integer,String>();
    Map<String,Float> expectedRanks = PageRankTest.parseExpectedRanks(PageRankTest.getWikiExampleExpectedRanks());

    File f = new File(System.getProperty("user.dir"), "input").getAbsoluteFile();
    if (f.exists())
    {
      f.delete();
    }

    FileWriter writer = new FileWriter(f);
    BufferedWriter bufferedWriter = new BufferedWriter(writer);

    for (String edge : edges)
    {
      String[] edgeParts = edge.split(" ");
      String source = edgeParts[0];
      String dest = edgeParts[1];
      if (!nodeIds.containsKey(source))
      {
        int id = nodeIds.size();
        nodeIds.put(source,id);
        nodeIdsReversed.put(id, source);
      }
      if (!nodeIds.containsKey(dest))
      {
        int id = nodeIds.size();
        nodeIds.put(dest,id);
        nodeIdsReversed.put(id, dest);
      }
      Integer sourceId = nodeIds.get(source);
      Integer destId = nodeIds.get(dest);

      StringBuffer sb = new StringBuffer();

      sb.append("1\t"); // topic
      sb.append(sourceId.toString() + "\t");
      sb.append(destId.toString() + "\t");
      sb.append("1.0\n"); // weight

      bufferedWriter.write(sb.toString());
    }

    bufferedWriter.close();

    test.runScript();
    Iterator<Tuple> tuples = test.getAlias("data_grouped3");

    System.out.println("Final node ranks:");
    int nodeCount = 0;
    while (tuples.hasNext())
    {
      Tuple nodeTuple = tuples.next();

      Integer topic = (Integer)nodeTuple.get(0);
      Integer nodeId = (Integer)nodeTuple.get(1);
      Float nodeRank = (Float)nodeTuple.get(2);

      assertEquals(1, topic.intValue());

      System.out.println(String.format("%d => %f", nodeId, nodeRank));

      Float expectedNodeRank = expectedRanks.get(nodeIdsReversed.get(nodeId));

      assertTrue(Math.abs(expectedNodeRank - nodeRank * 100.0f) < 0.1,
                 String.format("expected: %f, actual: %f", expectedNodeRank, nodeRank));

      nodeCount++;
    }

    assertEquals(nodeIds.size(),nodeCount);
  }
}
