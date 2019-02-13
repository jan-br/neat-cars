package de.jan.machinelearning.neat;

import de.jan.machinelearning.neat.core.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

public class GenomePrinter {
	
	public static void printGenome(IGenome genome, String path, File genomes) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(genome);
			objectOutputStream.writeInt(NodeGene.getIdCounter());
			objectOutputStream.writeInt(NodeConnection.getIdCounter());
			objectOutputStream.flush();
			FileOutputStream fileOutputStream = new FileOutputStream(genomes);
			fileOutputStream.write(byteArrayOutputStream.toByteArray());
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Random r = new Random();
		HashMap<Integer, Point> nodeGenePositions = new HashMap<Integer, Point>();
		int nodeSize = 20;
		int connectionSizeBulb = 6;
		int imageSize = 512;
		
		BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, imageSize, imageSize);
		
		g.setColor(Color.BLUE);
		for (INodeGene gene : genome.getNodeGenes()) {
			if (gene.getType() == NodeGeneType.INPUT) {
				float x = ((float)gene.getId()/((float)countNodesByType(genome, NodeGeneType.INPUT)+1f)) * imageSize;
				float y = imageSize-nodeSize/2;
				g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
				nodeGenePositions.put(gene.getId(), new Point((int)x,(int)y));
			} else if (gene.getType() == NodeGeneType.HIDDEN) {
				int x = r.nextInt(imageSize-nodeSize*2)+nodeSize;
				int y = r.nextInt(imageSize-nodeSize*3)+(int)(nodeSize*1.5f);
				g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
				nodeGenePositions.put(gene.getId(), new Point((int)x,(int)y));
			} else if (gene.getType() == NodeGeneType.OUTPUT) {
				int x = r.nextInt(imageSize-nodeSize*2)+nodeSize;
				int y = nodeSize/2;
				g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
				nodeGenePositions.put(gene.getId(), new Point((int)x,(int)y));
			}
		}
		
		g.setColor(Color.BLACK);
		for (INodeConnection gene : genome.getNodeGenes().stream().flatMap(nodeGene -> nodeGene.getNodeConnections().stream()).collect(Collectors.toList())) {
			if (!gene.isEnabled()) {
				continue;
			}
			Point inNode = nodeGenePositions.get(gene.getInputNodeGeneId());
			Point outNode = nodeGenePositions.get(gene.getOutputNodeGeneId());
			
			Point lineVector = new Point((int)((outNode.x - inNode.x) * 0.95f), (int)((outNode.y - inNode.y) * 0.95f));
			
			g.drawLine(inNode.x, inNode.y, inNode.x+lineVector.x, inNode.y+lineVector.y);
			g.fillRect(inNode.x+lineVector.x-connectionSizeBulb/2, inNode.y+lineVector.y-connectionSizeBulb/2, connectionSizeBulb, connectionSizeBulb);
			g.drawString(""+gene.getWeight(), (int)(inNode.x+lineVector.x*0.25f+5), (int)(inNode.y+lineVector.y*0.25f));
		}
		
		g.setColor(Color.WHITE);
		for (INodeGene nodeGene : genome.getNodeGenes()) {
			Point p = nodeGenePositions.get(nodeGene.getId());
			g.drawString(""+nodeGene.getId(), p.x, p.y);
		}
		
		
		try {
			File file = new File(path);
			file.getParentFile().mkdirs();
			if(!file.exists()){
				file.createNewFile();
			}
			ImageIO.write(image, "PNG", file);
		} catch (Exception e) {
		}
	}
	
	private static int countNodesByType(IGenome genome, NodeGeneType type) {
		int c = 0;
		for (INodeGene node : genome.getNodeGenes()) {
			if (node.getType() == type) {
				c++;
			}
		}
		return c;
	}

}
