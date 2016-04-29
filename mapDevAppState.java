/*
 * Third revision
 * The map dev tool will be split into appStates using TestAppStateLifeCycle
 * In this revision, we want to bring npc mobs (enemies/targets) and begin spawn system
 * Ray-cast bullet projectiles have been refactored in this revision
 * There is a flag to check for when the mobs have been reduced to zero
 * Removed monkeybrains AI control. Directly implementing SeekBehavior
 * An entity zone structure will be needed soon, frame rate is rough at start
 * 
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package MapDev;

import com.cubes.BlockChunkControl;
import com.cubes.BlockChunkListener;
import com.cubes.BlockTerrainControl;
import com.cubes.CubesSettings;
import com.cubes.Vector3Int;
import com.cubes.test.CubesTestAssets;
import com.jme3.ai.agents.util.control.MonkeyBrainsAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  Tests the app state lifecycles.
 *
 *  @author    Paul Speed
 */
public class mapDevAppState_r3 extends SimpleApplication {

    public static void main(String[] args){
        mapDevAppState_r3 app = new mapDevAppState_r3();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Global initialize method
        cam.setLocation(new Vector3f(-16.6f, 46, 97.6f));
        cam.lookAtDirection(new Vector3f(0.68f, -0.47f, -0.56f), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(15);
        System.out.println("Attaching test state.");
        stateManager.attach(new TestState());        
    }

    @Override
    public void simpleUpdate(float tpf) {
    
        if(stateManager.getState(TestState.class) != null) {
            //System.out.println("Detaching test state."); 
            //stateManager.detach(stateManager.getState(TestState.class));
            //System.out.println("Done"); 
        }        
    }
    
    // Adding ActorClassDev into global class-level for use in AppState
    //The ActorControl class encapsulates the ActorSetup method and related data
    class ActorControl {
        //Skeleton nodes
        public Node handle;
        public Node root;
        public Node body;
        public Node rLeg;
        public Node lLeg;
        public Node rArm;
        public Node lArm;
        public Node head;
        
        //r2 -- new Camera posNode and lookNode
        //Node camPos;
        //Node camLook;
        //Boolean ownCamera;
        
        //Box members for actors
        Box legBox;
        Box bodyBox;
        Box armBox;
        Box headBox;
        
        //Geometies for visuals
        Geometry lLegGeom;
        Geometry rLegGeom;
        Geometry bodyGeom;
        Geometry lArmGeom;
        Geometry rArmGeom;
        Geometry headGeom;
        
        //Material data
        Material legMat;
        Material bodyMat;
        Material armMat;
        Material headMat;
        
        //AnimControl and channel
        //public AnimChannel channel;
        //public AnimControl control;
        
        //r2 -- new Weapons Node, Box, and Geometry
        public Node weapon;
        Box weaponBox;
        Geometry weaponGeom;
        
        public Node target_pivot;
        
        public void setRootPos( Vector3f pos ) {
            this.handle.setLocalTranslation(pos);
        }
        
        public void setRootRot( Vector3f dir, Vector3f left ) {
            Quaternion qRot = cam.getRotation().clone();
            float angles[] = new float[3]; 
            qRot.toAngles(angles);
            angles[0] = 0f;
            angles[2] = 0f;
            qRot.fromAngles(angles);
            this.root.setLocalRotation(qRot);
        }
                
        ActorControl() {
            this.legMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            this.bodyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            this.armMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            this.headMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
            this.legMat.setColor("Color", ColorRGBA.DarkGray);
            this.bodyMat.setColor("Color", ColorRGBA.Blue);
            this.armMat.setColor("Color", ColorRGBA.Brown);
            this.headMat.setColor("Color", ColorRGBA.Brown);
            
            this.initActor();
        }
        
        public void initActor(){
            //Create the nodes that are used for joints
            this.root = new Node("root");
            this.body = new Node("body");
            this.rLeg = new Node("rLeg");
            this.lLeg = new Node("lLeg");
            this.rArm = new Node("rArm");
            this.lArm = new Node("lArm");
            this.head = new Node("head");
            this.handle = new Node("handle");
                                    
            //r2 -- Add a weapons node and create empty target node
            this.weapon = new Node("weapon");
            this.target_pivot = new Node("target");
            
            //Create the relationships between the joint nodes
            this.root.attachChild(this.body);
            this.handle.attachChild(this.root);
            this.body.attachChild(this.lLeg);
            this.body.attachChild(this.rLeg);
            this.body.attachChild(this.lArm);
            this.body.attachChild(this.rArm);
            this.body.attachChild(this.head);
            this.rArm.attachChild(this.weapon);
        
            //Move joints to local positions
            this.body.move(0,3f,0);
            this.lLeg.move(0.5f,0,0);
            this.rLeg.move(-0.5f,0,0);
            this.lArm.move(1.5f,3f,0);
            this.rArm.move(-1.5f,3f,0);
            this.head.move(0,3f,0);
            this.weapon.move(0,-3f,0.75f);
        
        
            //Create the physical dimensions of the actor 'minecraft-style'
            this.legBox = new Box(0.5f, 1.5f, 0.5f);
            this.bodyBox = new Box(1, 1.5f, 0.5f);
            this.armBox = new Box(0.5f, 1.5f, 0.5f);
            this.headBox = new Box(1, 1, 1);
            this.weaponBox = new Box(0.25f,0.75f,0.25f);
        
            //Create the visual elements and add materials
            this.lLegGeom = new Geometry("lLeg", this.legBox);
            this.rLegGeom = new Geometry("rLeg", this.legBox);
            this.bodyGeom = new Geometry("Body", this.bodyBox);
            this.lArmGeom = new Geometry("lArm", this.armBox);
            this.rArmGeom = new Geometry("rArm", this.armBox);
            this.headGeom = new Geometry("Head", this.headBox);
            this.weaponGeom = new Geometry("Weapon", this.weaponBox);
            
            //Set materials
            this.lLegGeom.setMaterial(this.legMat);
            this.rLegGeom.setMaterial(this.legMat);
            this.bodyGeom.setMaterial(this.bodyMat);
            this.lArmGeom.setMaterial(this.armMat);
            this.rArmGeom.setMaterial(this.armMat);
            this.headGeom.setMaterial(this.headMat);
            
            //TODO: Give weapons their own material
            this.weaponGeom.setMaterial(this.legMat);
        
            //Set the local transforms of geometry to align with joints properly
            this.lLegGeom.move(0,-1.5f,0);
            this.rLegGeom.move(0,-1.5f,0);
            this.bodyGeom.move(0,1.5f,0);
            this.lArmGeom.move(0,-1.5f,0);
            this.rArmGeom.move(0,-1.5f,0);
            this.headGeom.move(0,1f,0);
            this.weaponGeom.move(0,0,0);
        
            //Attach geometries to nodes
            this.body.attachChild(this.bodyGeom);
            this.lLeg.attachChild(this.lLegGeom);
            this.rLeg.attachChild(this.rLegGeom);
            this.lArm.attachChild(this.lArmGeom);
            this.rArm.attachChild(this.rArmGeom);
            this.head.attachChild(this.headGeom);
            this.weapon.attachChild(this.weaponGeom);
            
            Quaternion xRot = new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * 0,Vector3f.UNIT_X.clone());
            Quaternion yRot = new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * 0,Vector3f.UNIT_Z.clone());
            
            this.root.setLocalRotation(xRot.mult(yRot));
            //this.handle.getLocalRotation().lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
            
            this.root.scale(1f, 1f, 1f);
            
            //this.handle.setLocalTranslation(10f, 5f, 15f);
            
            rootNode.attachChild(this.handle);
        }
        
        public void initWeapon() {
            this.rArm.rotate(-90 * FastMath.DEG_TO_RAD, 0, 0);
        }
    }
    
    // There are a few extra components to make a 'mob' with ActorControl as an
    //interface. MonkeyBrains was used in earlier dev to implement seek behaviors
    public static int mob_count = 0;
    public static int mob_active = 0;
    
    class MobControl extends ActorControl{
        // jMonkey's CharacterControl gives physics to mob
        private CharacterControl mobControl;
        private Vector3f target;
        private int state;
        private int id;
        private float health;
        
        MobControl() {
            this.target = new Vector3f(0, 0, 0);
            this.state = 0;
            this.initMob();
            mob_count = mob_count + 1;
            this.id = mob_count;
            mob_active = mob_active + 1;
            this.health = 10;
        }

        private MobControl(Node shootables,int id) {
            this.target = new Vector3f(0, 0, 0);
            this.state = 0;
            this.id = id;
            this.initMob();
            shootables.attachChild(this.handle);
            mob_count = mob_count + 1;
            mob_active = mob_active + 1;
            this.health = 10;
        }
        
        public CharacterControl getControl() {
            return this.mobControl;
        }
        
        public void initSeekBehavior() {
            //this.target = target;
            this.state = 1;
        }
        
        //sets the target_pivot
        public void setTarget(Node targetNode) {
            this.target_pivot = targetNode;
        }
        
        public void changeState(int newState ) {
            switch(newState) {
                case -1:
                    //goto dead state
                    this.state = newState;
                    break;
                case 0:
                    //goto default state
                    this.state = newState;
                    break;
                case 1:
                    //run seek behavior
                    this.state = newState;
                    this.initSeekBehavior();
                    break;
                case 2:
                    //melee attack
            }
        }
        
        // This method runs a low-level fsm for each mob
        public void updateMob(Vector3f target,float tpf) {
            //System.out.println("updateMob called with state = " + this.state);
            switch(this.state) {
                case -1:
                    //dead state
                    this.handle.removeFromParent();
                    this.mobControl.destroy();
                    this.state = -2;
                    break;
                case 0:
                    //default state
                    if(this.health < 0f) {
                        this.changeState(-1);
                    }
                    Vector3f temp = target.subtract(this.handle.getLocalTranslation());
                    
                    this.mobControl.setWalkDirection(Vector3f.ZERO);
                    this.setRootPos(this.mobControl.getPhysicsLocation());
                    //System.out.println("id = "+ this.id + ". State = 0 (STANDBY) - Dist " + temp.length());
                    if( temp.length() < 50) {
                        
                        this.initSeekBehavior();
                    }
                    break;
                case 1:
                    //run seek behavior
                    Vector3f temp2 = target.subtract(this.handle.getLocalTranslation());
                    //System.out.println("id = "+ this.id + ". State = 1 (SEEK) - Dist " + temp2.length());
                    if( temp2.length() < 70) {
                        this.updateSeekBehavior(target, tpf);
                    } else {
                        this.changeState(0);
                    }                 
                    break;
                case 2:
                    //melee attack
            }
        }
        
        public void updateSeekBehavior( Vector3f target, float tpf ){ 
            if( this.state == 1) {
                this.target = target;
                Vector3f temp = target.subtract(this.handle.getLocalTranslation());
                temp.normalize();
                this.root.getLocalRotation().lookAt(temp.add(0,-4f,0), Vector3f.UNIT_Y.clone());
                
                //System.out.println("Rot angles = " + temp);
                temp.mult(tpf * 0.02f);
                //temp.add(this.handle.getLocalTranslation());
                temp.add(this.mobControl.getPhysicsLocation());
                this.mobControl.setWalkDirection(temp.mult(0.002f));
                this.setRootPos(new Vector3f(this.mobControl.getPhysicsLocation().getX(), this.mobControl.getPhysicsLocation().getY(), this.mobControl.getPhysicsLocation().getZ() ));
            }
            
            //this.setRootRot(angle, tpf);
        }
        
        public void doDamage(float damage) {
            this.health = this.health - damage;
            if(this.health < 0) {
                this.changeState(-1);
            }
        }
        
        public void setRootRot(float angle, float tpf) {
            Quaternion temp = new Quaternion();
            temp.fromAngleAxis(angle,new Vector3f(0,-1,0));
            this.root.rotate(0,angle, 0);
        }
        
        public void initMob() {
            // Call ancestor code from ActorControl
            this.initActor();
            //Refactor boss ActorControl into MobControl with MonkeyBrains
            CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2.5f,4.5f,1);
            this.mobControl = new CharacterControl(capsuleShape, 3f);//Step size is set in last argument
            this.mobControl.setJumpSpeed(20);
            this.mobControl.setFallSpeed(45);
            this.mobControl.setGravity(50);
            
            this.lLegGeom.setName("Mob/" + this.id + "/lLeg");
            this.rLegGeom.setName("Mob/" + this.id + "/rLeg");
            this.bodyGeom.setName("Mob/" + this.id + "/Body");
            this.lArmGeom.setName("Mob/" + this.id + "/lArm");
            this.rArmGeom.setName("Mob/" + this.id + "/rArm");
            this.headGeom.setName("Mob/" + this.id + "/Head");
            this.weaponGeom.setName("Mob/" + this.id + "/Weapon");
        }

        @Override
        public void setRootPos(Vector3f pos) {
            if( this.state == -1) {
                this.mobControl.setPhysicsLocation(pos);
                this.handle.setLocalTranslation(pos.add(0, -6, 0));
                //this.state = 0;
            } else {
                this.mobControl.setPhysicsLocation(pos);
                this.handle.setLocalTranslation(pos.add(0,-4,0));
            }
        }
    }
    
    // When we prepare for a second state, clean-up code will need to be implemented
    //for this test state.
    public class TestState extends AbstractAppState implements ActionListener{
        Application app;
        // Creating a mob_condition flag to track when mobs = 0
        public boolean mob_condition = false;
        public Node exit_level;
        public boolean level_exit_made = false;
        
        // This test state is used to refactor cubes map code from mapDevToolTest
        private BlockTerrainControl testTerrain;
        private Node testNode;
        private boolean setSpawnToggle;
        private int currentBlock;
        private BulletAppState bulletAppState;
        private RigidBodyControl phyTerrain;
        
        private ActorControl playerNode;
        private CharacterControl player;
        
        // MonkeyBrains AI components
        private MonkeyBrainsAppState brainsAppState;
        
        // Adding mobs
        private Node shootables;
        private ArrayList<MobControl> mobs;
        public Box fBullet;
        public ArrayList<Geometry> bullets;
        public ArrayList<Integer> bullet_remove;
        private Geometry mark;
        public Material fBullet_mat;
        public CollisionResults res;
        //private ActorControl bossNode;
        //private CharacterControl boss;
        
        //Camera data
        private Vector3f camDir = new Vector3f();
        private Vector3f camLeft = new Vector3f();
        
        //User Navigation using physics
        private Vector3f walkDirection = new Vector3f();
        private boolean left=false, right=false, up=false, down=false;
        
        
        // initControls directly refactored from mapDevToolTest
        private void initControls() {
            inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
            inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
            inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
            inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
            inputManager.addMapping("Shoot", new KeyTrigger (KeyInput.KEY_LSHIFT));
            inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addListener(this, "Left");
            inputManager.addListener(this, "Right");
            inputManager.addListener(this, "Up");
            inputManager.addListener(this, "Down");
            inputManager.addListener(this, "Shoot");
            inputManager.addListener(this, "Jump");
        }
        
        @Override
        public void onAction(String action, boolean value, float tpf){
            if (action.equals("Left")) {
                if (value) { left = true; } else { left = false; }
            } else if (action.equals("Right")) {
                if (value) { right = true; } else { right = false; }
            } else if (action.equals("Up")) {
                if (value) { up = true; } else { up = false; }
            } else if (action.equals("Down")) {
                if (value) { down = true; } else { down = false; }
            } else if (action.equals("Jump")) {
                player.jump();
            }
            if (action.equals("Shoot") && !value) {
                Geometry p = makeProjectile(cam.getLocation(),cam.getDirection());
                rootNode.attachChild(p);
                this.bullets.add(p);
            }
        }
        
        // Adding a projectile to attack a target with
        protected Geometry makeProjectile(Vector3f pos,Vector3f dir) {
            Sphere sphere = new Sphere(4, 4, 0.1f);
            Geometry projectile = new Geometry("projectile",sphere);
            projectile.setLocalTranslation(pos);
            Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat1.setColor("Color", ColorRGBA.Yellow);
            projectile.setMaterial(mat1);
            // Set lifetime and direction as userData
            projectile.setUserData("Lifetime", 2.0f);
            projectile.setUserData("Direction", dir);
            projectile.setUserData("Speed",45.5f);
            projectile.setUserData("Damage",2.5f);
            return projectile;
        }
        
        public void buildMap() {
            // CUBES ENGINE INITIALIZATION MOVED TO APPSTATE METHOD
            // Here we init cubes engine
            CubesTestAssets.registerBlocks();
            CubesSettings blockSettings = CubesTestAssets.getSettings(this.app);
            blockSettings.setBlockSize(5);
            
            testTerrain = new BlockTerrainControl(CubesTestAssets.getSettings(this.app), new Vector3Int(2, 1, 2));
            testNode = new Node();
            testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(32, 1, 32), CubesTestAssets.BLOCK_STONE);
            testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(32, 4, 1), CubesTestAssets.BLOCK_WOOD);
            testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(1, 4, 32), CubesTestAssets.BLOCK_WOOD);
            testTerrain.setBlockArea(new Vector3Int(31, 0, 0), new Vector3Int(1, 4, 32), CubesTestAssets.BLOCK_WOOD);
            testTerrain.setBlockArea(new Vector3Int(0, 0, 31), new Vector3Int(32, 4, 1), CubesTestAssets.BLOCK_WOOD);
            
            testTerrain.setBlockArea(new Vector3Int(0, 0, 24), new Vector3Int(10, 4, 3), CubesTestAssets.BLOCK_GRASS);
            testTerrain.setBlockArea(new Vector3Int(10, 0, 10), new Vector3Int(14, 6, 14), CubesTestAssets.BLOCK_GRASS);
            
            //Add voxel world/map to collisions
            testTerrain.addChunkListener(new BlockChunkListener(){
                @Override
                public void onSpatialUpdated(BlockChunkControl blockChunk){
                    Geometry optimizedGeometry = blockChunk.getOptimizedGeometry_Opaque();
                    phyTerrain = optimizedGeometry.getControl(RigidBodyControl.class);
                    if(phyTerrain == null){
                        phyTerrain = new RigidBodyControl(0);
                        optimizedGeometry.addControl(phyTerrain);
                        bulletAppState.getPhysicsSpace().add(phyTerrain);
                    }
                    phyTerrain.setCollisionShape(new MeshCollisionShape(optimizedGeometry.getMesh()));
                }
            });
            
            testNode.addControl(testTerrain);
            rootNode.attachChild(testNode);
        }
        
        public Vector3f spawnPoint( Vector3Int pos) {
            return new Vector3f((pos.getX() * 3f) + 1.5f, 7.5f, (pos.getZ() * 3f) + 1.5f);
        }
        
        public void initMobs() {
            //Refactor boss ActorControl into MobControl
            this.mobs = new ArrayList<MobControl>();
            
            Vector3f mobPos = spawnPoint(new Vector3Int(27,0,27));
            MobControl tempMob1 = new MobControl(this.shootables,1);
            tempMob1.setRootPos(mobPos);
            tempMob1.setTarget(this.playerNode.handle);
            tempMob1.id = 1;
            
            Vector3f mobPos2 = spawnPoint(new Vector3Int(29,0,29));
            MobControl tempMob2 = new MobControl(this.shootables,2);
            tempMob2.setRootPos(mobPos2);
            tempMob2.setTarget(this.playerNode.handle);
            tempMob2.id = 2;
            
            Vector3f mobPos3 = spawnPoint(new Vector3Int(25,0,25));
            MobControl tempMob3 = new MobControl(this.shootables,3);
            tempMob3.setRootPos(mobPos3);
            tempMob3.setTarget(this.playerNode.handle);
            tempMob3.id = 3;
            
            Vector3f mobPos4 = spawnPoint(new Vector3Int(6,0,6));
            MobControl tempMob4 = new MobControl(this.shootables,4);
            tempMob4.setRootPos(mobPos4);
            tempMob4.setTarget(this.playerNode.handle);
            tempMob4.id = 4;
            
            Vector3f mobPos5 = spawnPoint(new Vector3Int(4,0,4));
            MobControl tempMob5 = new MobControl(this.shootables,5);
            tempMob5.setRootPos(mobPos5);
            tempMob5.setTarget(this.playerNode.handle);
            tempMob5.id = 5;
            
            Vector3f mobPos6 = spawnPoint(new Vector3Int(2,0,2));
            MobControl tempMob6 = new MobControl(this.shootables,6);
            tempMob6.setRootPos(mobPos6);
            tempMob6.setTarget(this.playerNode.handle);
            tempMob6.id = 6;
            
            Vector3f mobPos7 = spawnPoint(new Vector3Int(29,0,6));
            MobControl tempMob7 = new MobControl(this.shootables,7);
            tempMob7.setRootPos(mobPos7);
            tempMob7.setTarget(this.playerNode.handle);
            tempMob7.id = 7;
            
            Vector3f mobPos8 = spawnPoint(new Vector3Int(27,0,4));
            MobControl tempMob8 = new MobControl(this.shootables,8);
            tempMob8.setRootPos(mobPos8);
            tempMob8.setTarget(this.playerNode.handle);
            tempMob8.id = 8;
            
            Vector3f mobPos9 = spawnPoint(new Vector3Int(25,0,2));
            MobControl tempMob9 = new MobControl(this.shootables,9);
            tempMob9.setRootPos(mobPos9);
            tempMob9.setTarget(this.playerNode.handle);
            tempMob9.id = 9;
                       
            this.bulletAppState.getPhysicsSpace().add(tempMob1.getControl());
            this.bulletAppState.getPhysicsSpace().add(tempMob2.getControl());
            this.bulletAppState.getPhysicsSpace().add(tempMob3.getControl());
            this.mobs.add(tempMob1);
            this.mobs.add(tempMob2);
            this.mobs.add(tempMob3);
            
            this.bulletAppState.getPhysicsSpace().add(tempMob4.getControl());
            this.bulletAppState.getPhysicsSpace().add(tempMob5.getControl());
            this.bulletAppState.getPhysicsSpace().add(tempMob6.getControl());
            this.mobs.add(tempMob4);
            this.mobs.add(tempMob5);
            this.mobs.add(tempMob6);
            
            this.bulletAppState.getPhysicsSpace().add(tempMob7.getControl());
            this.bulletAppState.getPhysicsSpace().add(tempMob8.getControl());
            this.bulletAppState.getPhysicsSpace().add(tempMob9.getControl());
            this.mobs.add(tempMob7);
            this.mobs.add(tempMob8);
            this.mobs.add(tempMob9);
            
            this.mob_condition = false;
        }
        
        @Override
        public void initialize(AppStateManager stateManager, Application app) {
            // This method loads the state
            super.initialize(stateManager, app);
            System.out.println("Initialization");
            
            this.app = app;
            
            level_exit_made = false;
            
            // Prepare the physics engine
            this.bulletAppState = new BulletAppState();
            stateManager.attach(bulletAppState);
            shootables = new Node();
            bullets = new ArrayList<Geometry>();
            bullet_remove = new ArrayList<Integer>();
            res = new CollisionResults();
            
            fBullet = new Box(0.5f,0.5f,0.5f); 
            fBullet_mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
            fBullet_mat.setColor("Color", ColorRGBA.Red);
            
            // CUBES ENGINE INITIALIZATION HAS BEEN MOVED TO APPSTATE METHOD
            this.buildMap();
            
            // ACTORCONTROL INTIALIZATION CAN BE MOVED TO APPSTATE METHOD
            //Set up first-person view with collisions
            CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2f,4.5f,1);
            this.player = new CharacterControl(capsuleShape, 3f);//Step size is set in last argument
            this.player.setJumpSpeed(20);
            this.player.setFallSpeed(45);
            this.player.setGravity(50);
            
            Vector3f playerPos = spawnPoint(new Vector3Int(2,0,29));
            this.player.setPhysicsLocation(playerPos);
            this.bulletAppState.getPhysicsSpace().add(player);
            
            this.playerNode = new ActorControl();
            //this.playerNode.setRootPos(new Vector3f(player.getPhysicsLocation().getX(), player.getPhysicsLocation().getY() - 6f, player.getPhysicsLocation().getZ() ));
            this.playerNode.setRootPos(playerPos.subtract(0, 6, 0));
            this.playerNode.initWeapon();
            
            //testNode.setLocalTranslation(40, 0, 0);
            cam.setLocation(playerPos);
            cam.lookAtDirection(new Vector3f(1, 0, 0), Vector3f.UNIT_Y);
            flyCam.setMoveSpeed(15);
            
            this.initMobs();
            
            rootNode.attachChild(shootables);
            
            initMark();
            this.initControls();
        }
 
        @Override
        public void stateAttached(AppStateManager stateManager) {
            super.stateAttached(stateManager);
            System.out.println("Attached");
        }
        
        // Refactored parts of update code into separate methods
        public void updatePlayer() {
            //System.out.println("update");
            camDir.set(cam.getDirection()).multLocal(0.3f);
            camLeft.set(cam.getLeft()).multLocal(0.2f);
        
            //initialize the walkDirection value so it can be recalculated
            walkDirection.set(0,0,0);
            if (left) {
                walkDirection.addLocal(camLeft);
            }
            if (right) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (up) {
                walkDirection.addLocal(camDir);
            }
            if (down) {
                walkDirection.addLocal(camDir.negate());
            }
            player.setWalkDirection(walkDirection);
            //player.get
            cam.setLocation(player.getPhysicsLocation().add(0,3f,0));
            playerNode.setRootPos(new Vector3f(player.getPhysicsLocation().getX(), player.getPhysicsLocation().getY() - 4.5f, player.getPhysicsLocation().getZ() ));
            playerNode.setRootRot(camDir, camLeft);
        }
 
        public void updateMobs(float tpf) {
            Vector3f temp = player.getPhysicsLocation();
            for(int i = 0;i < this.mobs.size();i++) {
                this.mobs.get(i).updateMob(temp,tpf);
            }
        }
        
        public void updateProjectiles(float tpf) {
            Vector3f tVect;
            float pLife;
            float pSpeed;
            float pDamage;
            bullet_remove.clear();
            
            if(bullets.size() > 0) {
                
                // 1. Prepare CollisionResults
            CollisionResults res = new CollisionResults();
            for(int i = 0; i < bullets.size();i++) {
                //For each projectile that is alive, move, then we will cast rays
                pLife = bullets.get(i).getUserData("Lifetime");
                if(pLife > 0.0) {
                    // Get temp Vector and move projectile, subtract life
                    pSpeed = bullets.get(i).getUserData("Speed");
                    tVect = (Vector3f)bullets.get(i).getUserData("Direction");
                    pDamage = bullets.get(i).getUserData("Damage");
                    for(int forward = 0; forward < 3; forward++) {
                        bullets.get(i).move( tVect.mult(tpf * pSpeed));
                        pLife = pLife - tpf;
                        bullets.get(i).setUserData("Lifetime", pLife);
                        //Re-using onAction method from sample 8
                        //2. Obtain the Ray the use. Need local translation and direction cast from user data
                        Ray r = new Ray(bullets.get(i).getLocalTranslation(),(Vector3f)bullets.get(i).getUserData("Direction"));
                        // 3. Collect intersections between Ray and Shootables in results list
                        shootables.collideWith(r,res);
                        // 4. Print the results (I want to anylize data instead)
                        for (int j = 0; j < res.size(); j++) {
                            // For each hit, we know distance, impact point, name of geometry.
                            float dist = res.getCollision(j).getDistance();
                            Vector3f pt = res.getCollision(j).getContactPoint();
                            //Here, we check the dist and point to check if we can count
                            //this as a hit
                            //----Encountering an occaisional OUT-OF-BOUNDS ERROR at this if statement (random, few times)
                            if( dist < 0.3f && bullets.get(i) != null) {
                                    //Register a hit, then remove the projectile
                                System.out.println("Hit confirmed at : " + pt);
                                // Instead of removing a bullet before the full list is ran, build a queue
                                    //bullets.get(i).removeFromParent();
                                //bullets.remove(i);
                                bullet_remove.add(i);
                                //In the future, we will subtract hit points from the shootable
                                // 5. USe the results (mark the last spot hit)
                                if (res.size() > 0) {
                                        // The closest collision point is what was truly hit:
                                    CollisionResult closest = res.getClosestCollision();
                                
                                        // Let's interact - we mark the hit with a red dot.
                                    mark.setLocalTranslation(closest.getContactPoint());
                                    System.out.println(closest.getGeometry().getName());
                                    //we could parse the name for the id here
                                    // info_data[ ActorType, Id, Geom ]
                                    String info = closest.getGeometry().getName();
                                    String info_data[] = info.split("/");
                                    int mob_id = Integer.parseInt(info_data[1]);
                                    for(int test = 0;test < this.mobs.size();test++) {
                                        if(this.mobs.get(test).id == mob_id) {
                                            System.out.println("Mob damage " + mob_id + " damage = " + pDamage);
                                            mobs.get(test).doDamage(pDamage);
                                            System.out.println("Health Remaining " + this.mobs.get(test).health);
                                        }
                                    }
                                    rootNode.attachChild(mark);
                                } else {
                                    // No hits? Then remove the red mark.
                                    rootNode.detachChild(mark);
                                }
                            }
                            //End of for loop (j)
                        }
                    }
                } else {
                    //If lifetime is less than 0.0, then we should release the projectile
                    bullet_remove.add(i);
                }             
                //End of for loop (i)
            }
            }
            //Remove all bullets that were marked for removal
            if(bullet_remove.size() > 0) {
                for(int b = 0; b < bullet_remove.size(); b++) {
                    bullets.get(bullet_remove.get(b)).removeFromParent();
                    bullets.remove(bullet_remove.get(b));
                }
            }
        }
        
        /** A red ball that marks the last spot that was "hit" by the "shot". */
        protected void initMark() {
            Sphere sphere = new Sphere(30, 30, 0.2f);
            mark = new Geometry("BOOM!", sphere);
            Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mark_mat.setColor("Color", ColorRGBA.Red);
            mark.setMaterial(mark_mat);
        }
        
        
        
        public void createExit() {
            if(this.level_exit_made == false) {
                this.exit_level = new Node("Exit");
                ParticleEmitter level_complete = new ParticleEmitter("Level complete",Type.Point, 30);
                level_complete.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
                level_complete.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
                level_complete.getParticleInfluencer().setInitialVelocity(new Vector3f(0,2,0));
                level_complete.setStartSize(1.5f);
                level_complete.setEndSize(0.1f);
                level_complete.setGravity(0,-8,0);
                level_complete.setLowLife(0.5f);
                level_complete.setHighLife(3f);
                level_complete.getParticleInfluencer().setVelocityVariation(0.3f);
                this.exit_level.attachChild(level_complete);
                rootNode.attachChild(this.exit_level);
                
                this.testTerrain.removeBlockArea(new Vector3Int(10,1,10), new Vector3Int(7,4,20));
                this.testTerrain.updateSpatial();
                this.level_exit_made = true;
            }
        }
        
        public float timer_countdown = 3;
        @Override // The appState's update method
        public void update(float tpf) {
            super.update(tpf);
            this.updatePlayer();
            updateProjectiles(tpf);
            updateMobs(tpf);
            //this.createExit();
            if(mobs.isEmpty()) {
                if(this.level_exit_made == false) {
                    mob_condition = true;
                    this.createExit();
                    System.out.println("Mobs clear");
                }
                timer_countdown = timer_countdown - tpf;                
            }
            
        }

        @Override
        public void render(RenderManager rm) {
            super.render(rm);
            //System.out.println("render");
        }

        @Override
        public void postRender() {
            super.postRender();
            //System.out.println("postRender");
        }

        @Override
        public void stateDetached(AppStateManager stateManager) {
            super.stateDetached(stateManager);
            System.out.println("Detached");
        }
 
        @Override
        public void cleanup() {
            // This method unloads the state
            super.cleanup();
            System.out.println("Cleanup"); 
            
            // Remove the input mappings that are associated with this state
            inputManager.deleteMapping("set_block");
            inputManager.deleteMapping("remove_block");
            inputManager.deleteMapping("change_block");
            inputManager.deleteMapping("save_block");
            inputManager.deleteMapping("load_block");
            inputManager.deleteMapping("toggle_spawn");
            
            // Deactivate the BlockTerrain and detach Terrain Node from the scene
            testNode.removeControl(testTerrain);
            rootNode.detachChild(testNode);
        } 
    }    
}
