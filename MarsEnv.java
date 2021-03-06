import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class MarsEnv extends Environment {

    public static final int GSize = 9; // grid size
    public static final int GARB  = 8; // garbage code in grid model
	public static final int GARBVIDRO  = 16;
	public static final int GARBPLASTICO  = 32;
	public static final int GARBPAPEL  = 64;
	public static final int GARBORGANICO  = 128;
	
    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    pg = Literal.parseLiteral("pick(garb)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
	public static final Literal g1v = Literal.parseLiteral("garbagev(r1)");
	public static final Literal g1pl = Literal.parseLiteral("garbagepl(r1)");
	public static final Literal g1pa = Literal.parseLiteral("garbagepa(r1)");
	public static final Literal g1o = Literal.parseLiteral("garbageo(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");
	public static final Literal g3 = Literal.parseLiteral("garbage(r3)");
	public static final Literal g4 = Literal.parseLiteral("garbage(r4)");
	public static final Literal g5 = Literal.parseLiteral("garbage(r5)");
	public static final Literal g6 = Literal.parseLiteral("garbage(r6)");
	
    static Logger logger = Logger.getLogger(MarsEnv.class.getName());

    private MarsModel model;
    private MarsView  view;
    
    @Override
    public void init(String[] args) {
        model = new MarsModel();
        view  = new MarsView(model);
        model.setView(view);
        updatePercepts();
    }
    
    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(x,y);
            } else if (action.equals(pg)) {
                model.pickGarb();
            } else if (action.equals(dg)) {
                model.dropGarb();
            } else if (action.equals(bg)) {
                model.burnGarb();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updatePercepts();

        try {
            Thread.sleep(500);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }
    
    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();
        
        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
		Location r3Loc = model.getAgPos(2);
		Location r4Loc = model.getAgPos(3);
		Location r5Loc = model.getAgPos(4);
		Location r6Loc = model.getAgPos(5);
        
        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
		Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
		Literal pos4 = Literal.parseLiteral("pos(r4," + r4Loc.x + "," + r4Loc.y + ")");
		Literal pos5 = Literal.parseLiteral("pos(r5," + r5Loc.x + "," + r5Loc.y + ")");
		Literal pos6 = Literal.parseLiteral("pos(r6," + r6Loc.x + "," + r6Loc.y + ")");
		
        addPercept(pos1);
        addPercept(pos2);
		addPercept(pos3);
		addPercept(pos4);
		addPercept(pos5);
		addPercept(pos6);
        
        if (model.hasObject(GARB, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(GARB, r4Loc)) {
            addPercept(g4);
        }
		if (model.hasObject(GARBPAPEL, r1Loc)) {
            addPercept(g1pa);
        }
		if (model.hasObject(GARBPAPEL, r2Loc)) {
            addPercept(g2);
        }
		if (model.hasObject(GARBPLASTICO, r1Loc)) {
            addPercept(g1pl);
        }
		if (model.hasObject(GARBPLASTICO, r3Loc)) {
            addPercept(g3);
        }
		if (model.hasObject(GARBORGANICO, r1Loc)) {
            addPercept(g1o);
        }
		if (model.hasObject(GARBORGANICO, r5Loc)) {
            addPercept(g5);
        }
		if (model.hasObject(GARBVIDRO, r1Loc)) {
            addPercept(g1v);
        }
		if (model.hasObject(GARBVIDRO, r6Loc)) {
            addPercept(g6);
        }
    }

    class MarsModel extends GridWorldModel {
        
        public static final int MErr = 2; // max error in pick garb
        int nerr; // number of tries of pick garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage toxico or not
		boolean r1HasGarbVidro = false; // whether r1 is carrying garbage vidro or not
		boolean r1HasGarbPapel = false; // whether r1 is carrying garbage papel or not
		boolean r1HasGarbPlastico = false; // whether r1 is carrying garbage plastico or not
		boolean r1HasGarbOrganico = false; // whether r1 is carrying garbage organico or not

        Random random = new Random(System.currentTimeMillis());

        private MarsModel() {
            super(GSize, GSize, 6);
            
            // initial location of agents
            try {
                setAgPos(0, 0, 0);
            
                Location r2Loc = new Location(GSize/2, GSize/2);
                setAgPos(1, r2Loc);
				
				Location r3Loc = new Location(2,6);
                setAgPos(2, r3Loc);
				
				setAgPos(3, 2, 2);
				
				setAgPos(4, 6, 2);
				
				setAgPos(5, 6, 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // initial location of garbage
            add(GARB, 3, 0);
			add(GARBVIDRO, 1, 1);
            add(GARBPAPEL, GSize-1, 0);
			add(GARBPAPEL, 5, 7);
			add(GARB, 2, 4);
            add(GARBORGANICO, 1, 2);
            add(GARBPLASTICO, 0, GSize-2);
			add(GARBPLASTICO, 6, 3);
            add(GARB, GSize-1, GSize-1);
	
        }
        
        void nextSlot() throws Exception {
            Location r1 = getAgPos(0);
            r1.x++;
            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y++;
            }
            // finished searching the whole grid
            if (r1.y == getHeight()) {
                return;
            }
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); 
			setAgPos(2, getAgPos(2)); 
			setAgPos(3, getAgPos(3)); 
			setAgPos(4, getAgPos(4)); 
			setAgPos(5, getAgPos(5)); 
        }
        
        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x)
                r1.x++;
            else if (r1.x > x)
                r1.x--;
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); 
			setAgPos(2, getAgPos(2)); 
			setAgPos(3, getAgPos(3)); 
			setAgPos(4, getAgPos(4)); 
			setAgPos(5, getAgPos(5)); 
        }
        
        void pickGarb() {
            // r1 location has garbage
            if (model.hasObject(GARB, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(0));
                    nerr = 0;
                    r1HasGarb = true;
                } else {
                    nerr++;
                }
            }
			else if (model.hasObject(GARBVIDRO, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARBVIDRO, getAgPos(0));
                    nerr = 0;
                    r1HasGarbVidro = true;
                } else {
                    nerr++;
                }
            }
			else if (model.hasObject(GARBPAPEL, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARBPAPEL, getAgPos(0));
                    nerr = 0;
                    r1HasGarbPapel = true;
                } else {
                    nerr++;
                }
            }
			else if (model.hasObject(GARBPLASTICO, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARBPLASTICO, getAgPos(0));
                    nerr = 0;
                    r1HasGarbPlastico = true;
                } else {
                    nerr++;
                }
            }
			else if (model.hasObject(GARBORGANICO, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARBORGANICO, getAgPos(0));
                    nerr = 0;
                    r1HasGarbOrganico = true;
                } else {
                    nerr++;
                }
            }
        }
        void dropGarb() {
            if (r1HasGarb) {
                r1HasGarb = false;
                add(GARB, getAgPos(0));
            }
			else if (r1HasGarbVidro) {
                r1HasGarbVidro = false;
                add(GARBVIDRO, getAgPos(0));
            }
			else if (r1HasGarbPapel) {
                r1HasGarbPapel = false;
                add(GARBPAPEL, getAgPos(0));
            }
			else if (r1HasGarbPlastico) {
                r1HasGarbPlastico = false;
                add(GARBPLASTICO, getAgPos(0));
            }
			else if (r1HasGarbOrganico) {
                r1HasGarbOrganico = false;
                add(GARBORGANICO, getAgPos(0));
            }
			
        }
        void burnGarb() {
            // r4 location has garbage
            if (model.hasObject(GARB, getAgPos(3))) {
                remove(GARB, getAgPos(3));
            }else if (model.hasObject(GARBVIDRO, getAgPos(5))) { // r6 location has garbage
                remove(GARBVIDRO, getAgPos(5));
            }else if (model.hasObject(GARBPAPEL, getAgPos(1))) { // r2 location has garbage
                remove(GARBPAPEL, getAgPos(1));
            }else if (model.hasObject(GARBPLASTICO, getAgPos(2))) { // r3 location has garbage
                remove(GARBPLASTICO, getAgPos(2));
            }else if (model.hasObject(GARBORGANICO, getAgPos(4))) { // r5 location has garbage
                remove(GARBORGANICO, getAgPos(4));
            }
        }
    }
    
    class MarsView extends GridWorldView {

        public MarsView(MarsModel model) {
            super(model, "Mars World - Antonio 12200988 e Elanne 10101180", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
                case MarsEnv.GARB: drawGarb(g, x, y,Color.pink,"GT");  break;
				case MarsEnv.GARBVIDRO: drawGarb(g, x, y,Color.green,"GV");  break;
				case MarsEnv.GARBPLASTICO: drawGarb(g, x, y,Color.red,"GPL");  break;
				case MarsEnv.GARBPAPEL: drawGarb(g, x, y,Color.blue,"GPA");  break;
				case MarsEnv.GARBORGANICO: drawGarb(g, x, y,Color.white,"GO");  break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);
            c = Color.blue; //papel
            if (id == 0) {
                c = Color.yellow;
                if (((MarsModel)model).r1HasGarb || ((MarsModel)model).r1HasGarbPlastico || ((MarsModel)model).r1HasGarbPapel || 
				((MarsModel)model).r1HasGarbOrganico || ((MarsModel)model).r1HasGarbVidro) {
                    label += " - G";
                    c = Color.orange;
                }
            }else if (id == 2) { //plastico
                c = Color.red;
            }
			else if (id == 3) { //toxico
                c = Color.pink;
            }
			else if (id == 4) { //organico
                c = Color.black;
            }
			else if (id == 5) { //vidro
                c = Color.green;
            }
			
            super.drawAgent(g, x, y, c, -1);
            if (id == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);                
            }
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawGarb(Graphics g, int x, int y, Color c, String letra) {
            super.drawObstacle(g, x, y);
            g.setColor(c);
            drawString(g, x, y, defaultFont, letra);
        }

    }    
}
