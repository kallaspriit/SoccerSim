package ee.ut.robotex.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

import ee.ut.robotex.simulation.Simulation;

public class Renderer implements ImageObserver {
	private Simulation simulation;
	private float areaWidth = 5.3f;
	private float areaHeight = 3.8f;
	private int lastWidth = 0;
	private int lastHeight = 0;
    private float scale = 100.0f;
    float xMargin = 0.0f;
    float yMargin = 0.0f;
    private BufferedImage background;
    boolean showPhysics = true;
	
	public Renderer(Simulation simulation) {
		this.simulation = simulation;
	}

	protected void paint(Graphics2D g, int width, int height) {
		float fieldWidth = simulation.getFieldWidth();
		float fieldHeight = simulation.getFieldHeight();
		float fieldMargin = (areaWidth - fieldWidth) / 2.0f;
		
		if (width != lastWidth || height != lastHeight) {
			scale = Math.min(
	        	width / areaWidth,
	        	height / areaHeight
	        );
			
			xMargin = Math.max((width / 2.0f / scale) - areaWidth / 2.0f, 0.0f);
			yMargin = Math.max((height / 2.0f / scale) - areaHeight / 2.0f, 0.0f);
			
			updateBackground(g.getTransform(), width, height);
		}
		
		g.drawImage(background, 0, 0, width, height, this);
		
		// set scale from meters to screen space
		g.scale(scale, scale);
		g.translate(xMargin + fieldMargin, yMargin + fieldMargin);
		
		g.setColor(new Color(0, 0, 0));
		g.setStroke(new BasicStroke(1.0f / scale));
		
		Font font = new Font("Consolas", Font.PLAIN, 1);
        g.setFont(font.deriveFont(0.5f));
		
		List<Body> bodies = simulation.getBodies();
		
		for (Body body : bodies) {
			Graphics2D g2 = (Graphics2D)g.create();
			
			Fixture fixture = body.getFixtureList();
			ShapeType type = fixture.getShape().getType();
			
			//AffineTransform lastTansform = g.getTransform();
			
			g2.translate(body.getPosition().x, body.getPosition().y);
			g2.rotate(body.getAngle());
			
			// let the body paint itself if possible
			Object userObject = body.getUserData();
			
			Graphics2D g3 = (Graphics2D)g2.create();
			
			if (userObject instanceof Paintable) {
				((Paintable)userObject).paint(g3);
			}
			
			if (showPhysics) {
				Graphics2D g4 = (Graphics2D)g2.create();
				
				// change color based on body type
				if (fixture.isSensor()) {
					g4.setColor(new Color(0, 200, 0));
				} else if (body.getType() == BodyType.DYNAMIC) {
					g4.setColor(new Color(200, 0, 0));
				} else {
					g4.setColor(new Color(200, 200, 200));
				}
				
				if (type == ShapeType.CIRCLE) {
					paintCircle(g4, fixture);
				} else if (type == ShapeType.POLYGON) {
					paintPolygon(g4, fixture);
				}
				
				//g.setTransform(lastTansform);
				
				/*
				// draw AABB
				AABB aabb = fixture.getAABB();
				
				Rectangle2D aabbRect = new Rectangle2D.Float(aabb.lowerBound.x, aabb.lowerBound.y, Math.abs(aabb.upperBound.x - aabb.lowerBound.x), Math.abs(aabb.upperBound.y - aabb.lowerBound.y));
				
				g.setColor(new Color(200, 200, 200));
				g.draw(aabbRect);
				*/
			}
			
			lastWidth = width;
			lastHeight = height;
		}
		
		float goalWidth = simulation.getGoalWidth();
		float goalDepth = simulation.getGoalDepth();
		
		// draw left score
        g.setColor(new Color(220, 220, 0));
        g.drawString(Integer.toString(simulation.getBlueScore()), -goalDepth + goalDepth / 9.0f, fieldHeight / 2.0f + goalWidth / 5.0f);
        
        // draw right score
        g.setColor(new Color(0, 0, 220));
        g.drawString(Integer.toString(simulation.getYellowScore()), fieldWidth + goalDepth / 9.0f, fieldHeight / 2.0f + goalWidth / 5.0f);
	}

	private void paintCircle(Graphics2D g, Fixture fixture) {
		CircleShape shape = (CircleShape)fixture.getShape();
		
		float radius = shape.m_radius;
		Ellipse2D ellipse = new Ellipse2D.Float(-radius, -radius, radius * 2.0f, radius * 2.0f);
		Line2D centerLine = new Line2D.Float(0, 0, radius, 0);
		
		g.draw(ellipse);
		g.draw(centerLine);
	}
	
	private void paintPolygon(Graphics2D g, Fixture fixture) {
		PolygonShape shape = (PolygonShape)fixture.getShape();
		Polygon2D polygon = new Polygon2D();
		
		for (int i = 0; i < shape.getVertexCount(); i++) {
			Vec2 vertex = shape.getVertex(i);
			
			polygon.addPoint(vertex.x, vertex.y);
		}
		
		g.draw(polygon);
	}
	
	void updateBackground(AffineTransform transform, int width, int height) {
		background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = background.createGraphics();
		
		g.scale(scale, scale);
		g.translate(xMargin, yMargin);
		
		Font font = new Font("Consolas", Font.PLAIN, 1);
        g.setFont(font.deriveFont(0.5f));
		
		// fill area
		g.setColor(new Color(0, 128, 0));
		g.fill(new Rectangle2D.Float(0, 0, areaWidth, areaHeight));
		
		float fieldWidth = simulation.getFieldWidth();
		float fieldHeight = simulation.getFieldHeight();
		float goalWidth = simulation.getGoalWidth();
		float lineWidth = 0.05f;
		float centerCircleRadius = 0.4f;
		float goalLineWidth = 0.3f;
		float goalLineDistance = 0.5f;
		float goalCircleRadius = 0.5f;
		float goalDepth = simulation.getGoalDepth();
		float fieldMargin = (areaWidth - fieldWidth) / 2.0f;
		
		g.translate(fieldMargin, fieldMargin);
		
		// draw the main field background
        g.setColor(new Color(0, 200, 0));
        g.fill(new Rectangle2D.Float(0, 0, fieldWidth, fieldHeight));
        
        // draw centerline
        g.setColor(new Color(255, 255, 255));
        g.fill(new Rectangle2D.Float(fieldWidth / 2.0f - lineWidth / 2.0f, 0, lineWidth, fieldHeight));
        
        // draw sidelines
        g.fill(new Rectangle2D.Float(0.0f, 0.0f, fieldWidth, lineWidth)); // top
        g.fill(new Rectangle2D.Float(0.0f, 0.0f, lineWidth, fieldHeight)); // left
        g.fill(new Rectangle2D.Float(0.0f, fieldHeight - lineWidth, fieldWidth, lineWidth)); // top
        g.fill(new Rectangle2D.Float(fieldWidth - lineWidth, 0.0f, lineWidth, fieldHeight)); // right
        
        // draw center circle
        g.setStroke(new BasicStroke(lineWidth));
        g.draw(new Ellipse2D.Float(
        	fieldWidth / 2.0f - centerCircleRadius,
        	fieldHeight / 2.0f - centerCircleRadius,
        	centerCircleRadius * 2.0f,
        	centerCircleRadius * 2.0f
        ));
        
        // draw left goal line
        g.fill(new Rectangle2D.Float(goalLineDistance, fieldHeight / 2.0f - goalLineWidth / 2.0f, lineWidth, goalLineWidth));
        g.draw(new Arc2D.Float(-goalLineDistance + lineWidth / 2.0f, fieldHeight / 2.0f - (goalLineWidth / 2.0f + goalCircleRadius), goalCircleRadius * 2.0f, goalCircleRadius * 2.0f, 0.0f, 90.0f, Arc2D.OPEN));
        g.draw(new Arc2D.Float(-goalLineDistance + lineWidth / 2.0f, fieldHeight / 2.0f - (goalLineWidth + lineWidth), goalCircleRadius * 2.0f, goalCircleRadius * 2.0f, 270.0f, 90.0f, Arc2D.OPEN));
        
        // draw right goal line
        g.fill(new Rectangle2D.Float(fieldWidth - goalLineDistance - lineWidth, fieldHeight / 2.0f - goalLineWidth / 2.0f, lineWidth, goalLineWidth));
        g.draw(new Arc2D.Float(fieldWidth - (goalCircleRadius + lineWidth / 2.0f), fieldHeight / 2.0f - (goalLineWidth / 2.0f + goalCircleRadius), goalCircleRadius * 2.0f, goalCircleRadius * 2.0f, 90.0f, 90.0f, Arc2D.OPEN));
        g.draw(new Arc2D.Float(fieldWidth - (goalCircleRadius + lineWidth / 2.0f), fieldHeight / 2.0f - (goalLineWidth + lineWidth), goalCircleRadius * 2.0f, goalCircleRadius * 2.0f, 180.0f, 90.0f, Arc2D.OPEN));
		
        // draw left goal
        g.setColor(new Color(0, 0, 220));
        g.fill(new Rectangle2D.Float(-goalDepth, fieldHeight / 2.0f - goalWidth / 2.0f, goalDepth, goalWidth));
        
        // draw right goal
        g.setColor(new Color(220, 220, 0));
        g.fill(new Rectangle2D.Float(fieldWidth, fieldHeight / 2.0f - goalWidth / 2.0f, goalDepth, goalWidth));
        
		g.dispose();
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return false;
	}
}
