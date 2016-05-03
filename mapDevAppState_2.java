/*
 * 2nd Generation base revision
 * The map dev tool will need to have an inbuilt editor, implementing hard-
 * coded level with spawnPoints and zoneMesh areas.
 * 
 * This software was built using jMonkeyEngine IDE Copyright (c) 2009-2012
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

import com.cubes.Block;
import com.cubes.BlockChunkControl;
import com.cubes.BlockChunkListener;
import com.cubes.BlockNavigator;
import com.cubes.BlockTerrainControl;
import com.cubes.CubesSettings;
import com.cubes.Vector3Int;
import com.cubes.test.CubesTestAssets;
import com.jme3.ai.agents.util.control.MonkeyBrainsAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.plugins.FileLocator;
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
import com.jme3.export.binary.BinaryExporter;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  Use case development of map system for DOOMVOX
 */
public class mapDevAppState_2 extends SimpleApplication {

    public static void main(String[] args){
        mapDevAppState_2 app = new mapDevAppState_2();
        AppSettings settings = new AppSettings(false);
        settings.setSettingsDialogImage("Interface/TitleSettings.png");
        app.setSettings(settings);
        settings.setTitle("Voxel Platform Dev - DoomVox Demo v2 r0");
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Global initialize method
        cam.setLocation(new Vector3f(23f, 26, 120f));
        cam.lookAtDirection(new Vector3f(0f, -0.78f,1f), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(15);
        System.out.println("Attaching test state.");
        stateManager.attach(new WorldBuilder());        
    }

    @Override
    public void simpleUpdate(float tpf) {
    
        if(stateManager.getState(WorldBuilder.class) != null) {
            //System.out.println("Detaching test state."); 
            
        }        
    }
    // The ActorControl class encapsulates data for player
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
        
        private CharacterControl mobControl;
        public Vector3f walkDirection = new Vector3f();
        public boolean left=false, right=false, up=false, down=false;
        private Vector3f camDir;
        private Vector3f camLeft;
        private boolean hasCam=false;
        
        //r2 -- new Weapons Node, Box, and Geometry
        public Node weapon;
        Box weaponBox;
        Geometry weaponGeom;
        
        public Node target_pivot;
                
        public void setRootPos( Vector3f pos ) {
            //this.mobControl.setPhysicsLocation(pos.add(0,0,0));
            this.handle.setLocalTranslation(pos.add(0,-3.5f,0));
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
        
        public void setRootRot(float angle) {
            Quaternion temp = new Quaternion();
            temp.fromAngleAxis(angle,new Vector3f(0,-1,0));
            //this.root.rotate(0,angle, 0);
            this.root.setLocalRotation(temp);
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
        
        public void setCamera() {
            cam.setLocation(this.handle.getLocalTranslation().add(0,5,0));
            cam.lookAtDirection(Vector3f.UNIT_X, Vector3f.UNIT_Y);
            flyCam.setMoveSpeed(15);
            this.hasCam = true;
            this.camDir = new Vector3f();
            this.camLeft = new Vector3f();
            this.walkDirection = new Vector3f();
        }
        
        // Refactored parts of update code into separate methods
        public void updatePlayer() {
            if(this.hasCam) {
                this.camDir.set(cam.getDirection()).multLocal(0.3f);
                this.camLeft.set(cam.getLeft()).multLocal(0.2f);
                //initialize the walkDirection value so it can be recalculated
                walkDirection.set(0,0,0);
                if (this.left) {
                    this.walkDirection.addLocal(this.camLeft);
                }
                if (this.right) {
                    this.walkDirection.addLocal(this.camLeft.negate());
                }
                if (this.up) {
                    this.walkDirection.addLocal(this.camDir);
                }
                if (this.down) {
                    this.walkDirection.addLocal(this.camDir.negate());
                }
                this.mobControl.setWalkDirection(this.walkDirection);
                //player.get
                cam.setLocation(this.mobControl.getPhysicsLocation().add(0,1.5f,0));
                this.setRootPos(this.mobControl.getPhysicsLocation().add(0,-0.75f,0));
                this.setRootRot(this.camDir, this.camLeft);
            }
            // If the actor control does not have ownership of cam, do nothing
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
            
            //Refactor ActorControl with Character Control
            CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2.5f,3f,1);
            this.mobControl = new CharacterControl(capsuleShape, 3f);//Step size is set in last argument
            this.mobControl.setJumpSpeed(20);
            this.mobControl.setFallSpeed(45);
            this.mobControl.setGravity(50);
            
            
            this.root.setLocalRotation(xRot.mult(yRot));
            //this.handle.getLocalRotation().lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
            
            this.root.scale(0.75f, 0.75f, 0.75f);
            
            //this.handle.setLocalTranslation(10f, 5f, 15f);
            
            rootNode.attachChild(this.handle);
        }
        
        public void initWeapon() {
            this.rArm.rotate(-90 * FastMath.DEG_TO_RAD, 0, 0);
        }
    }
    
    // There are a few extra components to make a 'mob' with ActorControl as an
    //interface.
    public static int mob_count = 0;
    public static int mob_active = 0;
    
    // The MobControl
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
            //The creation of the geometries is handled by superclass ActorControl
            this.lLegGeom.setName("Mob/" + this.id + "/lLeg");
            this.rLegGeom.setName("Mob/" + this.id + "/rLeg");
            this.bodyGeom.setName("Mob/" + this.id + "/Body");
            this.lArmGeom.setName("Mob/" + this.id + "/lArm");
            this.rArmGeom.setName("Mob/" + this.id + "/rArm");
            this.headGeom.setName("Mob/" + this.id + "/Head");
            this.weaponGeom.setName("Mob/" + this.id + "/Weapon"); 
            
            this.armMat.setColor("Color",ColorRGBA.Black);
            this.legMat.setColor("Color",ColorRGBA.Black);
            this.bodyMat.setColor("Color",ColorRGBA.Black);
            
            //Refactor boss ActorControl into MobControl with MonkeyBrains
            CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2.5f,4.5f,1);
            this.mobControl = new CharacterControl(capsuleShape, 3f);//Step size is set in last argument
            this.mobControl.setJumpSpeed(20);
            this.mobControl.setFallSpeed(45);
            this.mobControl.setGravity(50);
            
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
    
    // New Edit State
    public class WorldBuilder extends AbstractAppState implements ActionListener{
        public Application app;
        
        public BlockTerrainControl testTerrain;
        public Node testNode;
        public RigidBodyControl phyTerrain;
        public BulletAppState bulletAppState;
        
        public Node spawnPoint;
        public ArrayList<Node> spawnPointList;
        public ArrayList<Node> zoneList;
        public Node zoneRoot;
        
        public ActorControl player;
        public ArrayList<MobControl> mobList;
        public Node shootables;
        
        private BitmapText currentBlockDisplay;
        public String presentZone;
        private BitmapText currentZoneDisplay;
        private int currentBlock;
        
        public boolean setSpawnToggle;
        public int editorState;
        public Vector3Int tempAreaSet;
        public Vector3Int tempAreaPos;
        public float blockScale;
        public float halfBlockStep;
        
        public void initPlayer( boolean debugMode ) {
            if(debugMode) {
                this.player = new ActorControl();
                
                this.player.setRootPos( new Vector3f(spawnPointList.get(0).getLocalTranslation().add(0,3,0)));
                this.player.mobControl.setPhysicsLocation(spawnPointList.get(0).getLocalTranslation().add(0,2,0));
                float rot = spawnPointList.get(0).getUserData("Orientation");
                this.player.setRootRot(rot * FastMath.DEG_TO_RAD);
                this.bulletAppState.getPhysicsSpace().add(this.player.mobControl);
                this.player.setCamera();
                this.presentZone = new String("Zone 1");
            }
        }
        
        public boolean spawnNewMob(int spawnId) {
            if(spawnId < this.spawnPointList.size()) {
                MobControl mob = new MobControl();
                mob.setRootPos(new Vector3f(this.spawnPointList.get(spawnId).getLocalTranslation().add(0,4,0)));
                float rot = this.spawnPointList.get(spawnId).getUserData("Orientation");
                mob.setRootRot(rot * FastMath.DEG_TO_RAD,0);
                this.bulletAppState.getPhysicsSpace().add(mob.mobControl);
                this.mobList.add(mob);
                
                return true;
            } else {
                return false;
            }
        }
        
        // The initMobs() method creates the first set of mobs.
        public void initMobs( boolean debugMode ) {
            this.mobList = new ArrayList<MobControl>();
            if(debugMode) {
                MobControl mob = new MobControl();
                
                mob.setRootPos(new Vector3f(spawnPointList.get(1).getLocalTranslation().add(0,4,0)));
                float rot = spawnPointList.get(1).getUserData("Orientation");
                mob.setRootRot(rot * FastMath.DEG_TO_RAD,0);
                this.bulletAppState.getPhysicsSpace().add(mob.mobControl);
                mobList.add(mob);
                
                mob = new MobControl();
                
                mob.setRootPos(new Vector3f(spawnPointList.get(2).getLocalTranslation().add(0,4,0)));
                rot = spawnPointList.get(2).getUserData("Orientation");
                mob.setRootRot(rot * FastMath.DEG_TO_RAD,0);
                this.bulletAppState.getPhysicsSpace().add(mob.mobControl);
                mobList.add(mob);
            }
            this.initZoneAreas();
        }
        
        // Convert from Cubes engine block coordinates to World coordinates
        public Vector3f CubeToWorldVector(float pX, float pY, float pZ) {
            return new Vector3f((pX * this.blockScale) + (this.blockScale * 0.5f),(pY * this.blockScale) + this.blockScale,(pZ * this.blockScale) + (this.blockScale * 0.5f));
        }
        
        // Build the navmesh for zones
        public void initZoneAreas() {
            this.zoneList = new ArrayList<Node>();
            this.zoneRoot = new Node("Zone Root");
            
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); 
            mat.setColor("Color", new ColorRGBA(0,1,1,0.5f));
            mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
            
            Geometry zoneBox1 = new Geometry("Zone 1",new Box(12,1,12));
            Node zoneNode1 = new Node("Zone 1");
            zoneBox1.setMaterial(mat);
            zoneBox1.setQueueBucket(Bucket.Transparent);
            zoneNode1.attachChild(zoneBox1);
            this.zoneList.add(zoneNode1);
            this.zoneRoot.attachChild(zoneNode1);
            
            zoneNode1.setLocalTranslation(CubeToWorldVector(7.5f,0,47.5f));
            
            Geometry zoneBox2 = new Geometry("Zone 2",new Box(18f,1,6));
            Node zoneNode2 = new Node("Zone 2");
            zoneBox2.setMaterial(mat);
            zoneBox2.setQueueBucket(Bucket.Transparent);
            zoneNode2.attachChild(zoneBox2);
            this.zoneList.add(zoneNode2);
            this.zoneRoot.attachChild(zoneNode2);
            
            zoneNode2.setLocalTranslation(CubeToWorldVector(17.5f,0,47.5f));
            
            Geometry zoneBox2b = new Geometry("Zone 2b",new Box(6f,1,6));
            Node zoneNode2b = new Node("Zone 2b");
            zoneBox2b.setMaterial(mat);
            zoneBox2b.setQueueBucket(Bucket.Transparent);
            zoneNode2b.attachChild(zoneBox2b);
            this.zoneList.add(zoneNode2b);
            this.zoneRoot.attachChild(zoneNode2b);
            
            zoneNode2b.setLocalTranslation(CubeToWorldVector(21.5f,0,43.5f));
            
            Geometry zoneBox3 = new Geometry("Zone 3",new Box(24f,1,22.5f));
            Node zoneNode3 = new Node("Zone 3");
            zoneBox3.setMaterial(mat);
            zoneBox3.setQueueBucket(Bucket.Transparent);
            zoneNode3.attachChild(zoneBox3);
            this.zoneList.add(zoneNode3);
            this.zoneRoot.attachChild(zoneNode3);
            
            zoneNode3.setLocalTranslation(CubeToWorldVector(23.5f,0,34f));
            
            Geometry zoneBox4 = new Geometry("Zone 4",new Box(9f,1,9f));
            Node zoneNode4 = new Node("Zone 4");
            zoneBox4.setMaterial(mat);
            zoneBox4.setQueueBucket(Bucket.Transparent);
            zoneNode4.attachChild(zoneBox4);
            this.zoneList.add(zoneNode4);
            this.zoneRoot.attachChild(zoneNode4);
            
            zoneNode4.setLocalTranslation(CubeToWorldVector(12.5f,0,29.5f));
            
            Geometry zoneBox4b = new Geometry("Zone 4b",new Box(9f,1,22.5f));
            Node zoneNode4b = new Node("Zone 4b");
            zoneBox4b.setMaterial(mat);
            zoneBox4b.setQueueBucket(Bucket.Transparent);
            zoneNode4b.attachChild(zoneBox4b);
            this.zoneList.add(zoneNode4b);
            this.zoneRoot.attachChild(zoneNode4b);
            
            zoneNode4b.setLocalTranslation(CubeToWorldVector(6.5f,0,25f));
            
            Geometry zoneBox5 = new Geometry("Zone 5",new Box(36f,1,18f));
            Node zoneNode5 = new Node("Zone 5");
            zoneBox5.setMaterial(mat);
            zoneBox5.setQueueBucket(Bucket.Transparent);
            zoneNode5.attachChild(zoneBox5);
            this.zoneList.add(zoneNode5);
            this.zoneRoot.attachChild(zoneNode5);
            
            zoneNode5.setLocalTranslation(CubeToWorldVector(13.5f,0,11.5f));
            rootNode.attachChild(this.zoneRoot);
        }
        
        private void initGUI(){
            //SEt some default parameters for editor
            this.currentBlock = 0;
            this.setSpawnToggle = false;
            this.editorState = 0;
            this.tempAreaPos = new Vector3Int();
            this.tempAreaSet = new Vector3Int();
            
            //Crosshair
            BitmapText crosshair = new BitmapText(guiFont);
            crosshair.setText("+");
            crosshair.setSize(guiFont.getCharSet().getRenderedSize() * 2);
            crosshair.setLocalTranslation(
                    (settings.getWidth() / 2) - (guiFont.getCharSet().getRenderedSize() / 3 * 2),
                    (settings.getHeight() / 2) + (crosshair.getLineHeight() / 2), 0);
            guiNode.attachChild(crosshair);
            //Instructions
            BitmapText instructionsText1 = new BitmapText(guiFont);
            instructionsText1.setText("Movement: WASD");
            instructionsText1.setLocalTranslation(0, settings.getHeight(), 0);
            guiNode.attachChild(instructionsText1);
            BitmapText instructionsText2 = new BitmapText(guiFont);
            instructionsText2.setText("Stats Screen Toogle: F5");
            instructionsText2.setLocalTranslation(0, settings.getHeight() - instructionsText2.getLineHeight(), 0);
            guiNode.attachChild(instructionsText2);
            BitmapText instructionsText3 = new BitmapText(guiFont);
            instructionsText3.setText("Jump: Spacebar");
            instructionsText3.setLocalTranslation(0, settings.getHeight() - (2 * instructionsText3.getLineHeight()), 0);
            guiNode.attachChild(instructionsText3);
            
            BitmapText instructionsText4 = new BitmapText(guiFont);
            instructionsText4.setText("Quit: Esc");
            instructionsText4.setLocalTranslation(0, settings.getHeight() - (3 * instructionsText3.getLineHeight()), 0);
            guiNode.attachChild(instructionsText4);
            
            BitmapText instructionsText5 = new BitmapText(guiFont);
            instructionsText5.setText("Version: v2 r2");
            instructionsText5.setLocalTranslation(0, settings.getHeight() - (4 * instructionsText3.getLineHeight()), 0);
            guiNode.attachChild(instructionsText5);
            
            //CurrentBlock indicator
            currentBlockDisplay = new BitmapText(guiFont);
            //currentBlockDisplay.setText("Current Block: 0 - BLOCK_GRASS");
            //currentBlockDisplay.setLocalTranslation(0, settings.getHeight() - (5 * instructionsText3.getLineHeight()), 0);
            //guiNode.attachChild(currentBlockDisplay);
            
            //CurrentZone indicator
            currentZoneDisplay = new BitmapText(guiFont);
            currentZoneDisplay.setText("Current Zone: Zone 1");
            currentZoneDisplay.setLocalTranslation(0, settings.getHeight() - (5 * instructionsText3.getLineHeight()), 0);
            guiNode.attachChild(currentZoneDisplay);
       }
        
        public void scanNode() {
            ArrayList<Integer> data = testNode.getUserData("Block_ID_List");
            ArrayList<Vector3Int> pos = testNode.getUserData("Block_POS_List");
            for(int i = 0; i < data.size(); i++) {
                switch(data.get(i)){
                    case 0:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_GRASS);
                        break;
                    case 1:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_WOOD);
                        break;
                    case 2:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_WOOD_FLAT);
                        break;
                    case 3:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_BRICK);
                        break;
                    case 4:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_CONNECTOR_ROD);
                        break;
                    case 5:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_GLASS);
                        break;
                    case 6:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_STONE);
                        break;
                    case 7:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_STONE_PILLAR);
                        break;
                    case 8:
                        this.setVoxel(pos.get(i), CubesTestAssets.BLOCK_WATER);
                        break;
                }
            }
        }
        
        public void scanMap() {
            Block b;
            ArrayList<Integer> data = new ArrayList<Integer>();
            ArrayList<Vector3Int> pos = new ArrayList<Vector3Int>();
            
            for(int y = 0; y < 16; y++) {
                for(int x = 0; x < 32; x++) {
                    for( int z = 0; z < 32; z++) {
                        b = testTerrain.getBlock(x, y, z);
                        if( b.equals(CubesTestAssets.BLOCK_STONE)) {
                            // Store Stone value
                            data.add(6);
                            pos.add(new Vector3Int(x,y,z));
                        } else if(b.equals(CubesTestAssets.BLOCK_WOOD) ){
                            // Store Wood value
                            data.add(1);
                            pos.add(new Vector3Int(x,y,z));
                        } else if(b.equals(CubesTestAssets.BLOCK_GRASS) )  {
                            // Store Grass value
                            data.add(0);
                            pos.add(new Vector3Int(x,y,z));
                        } else if(b.equals(CubesTestAssets.BLOCK_BRICK) )  {
                            // Store Grass value
                            data.add(3);
                            pos.add(new Vector3Int(x,y,z));
                        }
                    }
                }
            }
            // After the array lists are populated, export the data somewhere
            this.testNode.setUserData("Block_ID_List", data);
            this.testNode.setUserData("Block_POS_List", pos);
        }
        
        //Saves terrain data via user data from testNode
        public void saveMap() {
            this.scanMap();
            String userHome = System.getProperty("user.home");
            BinaryExporter export = BinaryExporter.getInstance();
            File file = new File(userHome + "world.j3o");
            try {
                export.save(this.testNode, file);  
            } catch (IOException ex) {
                Logger.getLogger(mapDevAppState_2.class.getName()).log(Level.SEVERE, "File write error");
            }
        }
        
        public void loadMap() {
            rootNode.detachChild(testNode);
            testTerrain.removeBlockArea(new Vector3Int(0,1,0), new Vector3Int(32,15,32));
            String userHome = System.getProperty("user.home");
            assetManager.registerLocator(userHome,FileLocator.class);
            testNode = (Node)assetManager.loadModel("world.j3o");
            testNode.setName("loaded node");
            //Re-create the terrainNode
            this.scanNode();
            rootNode.attachChild(this.testNode);
        }
        
        // Helper method to add spawn point and associated debug markers, added defaultRot argument
        public void addSpawnPoint(float pX, float pY, float pZ,float defaultRot, boolean playerSpawn) {
            //this.testTerrain.setBlockArea( new Vector3Int(5,0,47), new Vector3Int(2,1,2), CubesTestAssets.BLOCK_WOOD);
            Geometry spawnBox = new Geometry("Spawn",new Box(1,2,1));
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");          
            
            if(playerSpawn) {
                //If playerSpawn == true, set the global spawn point
                this.spawnPoint = new Node("Player Spawn Point");
                this.spawnPoint.setUserData("Orientation",defaultRot);
                mat.setColor("Color", new ColorRGBA(0,0,1,0.5f));
                mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                spawnBox.setMaterial(mat);
                spawnBox.setQueueBucket(Bucket.Transparent);
                this.spawnPoint.attachChild(spawnBox);
                rootNode.attachChild(this.spawnPoint);
                this.spawnPoint.setLocalTranslation((pX * this.blockScale) + (this.blockScale * 0.5f),(pY * this.blockScale) + this.blockScale,(pZ * this.blockScale) + (this.blockScale * 0.5f));
                this.spawnPointList.add(this.spawnPoint);
            } else {
                //If playerSpawn == false, add a mob spawn point
                Node spawn = new Node("Mob Spawn Point");
                spawn.setUserData("Orientation",defaultRot);
                mat.setColor("Color", new ColorRGBA(0.5f,0.5f,0.5f,0.5f));
                mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                spawnBox.setMaterial(mat);
                spawnBox.setQueueBucket(Bucket.Transparent);
                spawn.attachChild(spawnBox);
                rootNode.attachChild(spawn);
                spawn.setLocalTranslation((pX * this.blockScale) + (this.blockScale * 0.5f),(pY * this.blockScale) + this.blockScale,(pZ * this.blockScale) + (this.blockScale * 0.5f));
                this.spawnPointList.add(spawn);
            }
            
        }
        
        // Helper method to create spawn points separate from the map
        public void initSpawnPoints() {
            this.spawnPointList = new ArrayList<Node>();
            
            //Player spawn point location (5,0,47)  In Ready Room
            this.addSpawnPoint(5.5f,0,47.5f,90, true);
            //Mob spawn point location (16,0,47)    In Zone 2
            this.addSpawnPoint(16.5f, 0, 47.5f, 270, false);          
            //Mob spawn point location (21,0,43)
            this.addSpawnPoint(21.5f, 0, 43.5f, 0,false);
            
            //Mob spawners in Zone 3
            this.addSpawnPoint(17.5f, 0, 37.5f, 90, false);
            this.addSpawnPoint(29.5f, 0, 37.5f, 90, false);
            this.addSpawnPoint(17.5f, 0, 29.5f, 270, false);
            
            //Mob spawners in Zone 4
            this.addSpawnPoint(12.5f, 0, 29.5f, 90, false);
            this.addSpawnPoint(6.5f, 0, 20.5f, 0, false);
            
            //Mob spawners in Zone 5
            this.addSpawnPoint(9.5f, 0, 6.5f, 0, false);
            this.addSpawnPoint(17.5f, 0, 6.5f, 0, false);
            this.addSpawnPoint(24.5f, 0, 8.5f, 270, false);
            this.addSpawnPoint(24.5f, 0, 11.5f, 270, false);
            this.addSpawnPoint(24.5f, 0, 14.5f, 270, false);
        }
        
        // Generate the map with Cubes engine
        public void buildMap() {
            // CUBES ENGINE INITIALIZATION MOVED TO APPSTATE METHOD
            // Here we init cubes engine
            CubesTestAssets.registerBlocks();
            CubesSettings blockSettings = CubesTestAssets.getSettings(this.app);
            blockSettings.setBlockSize(5);
            //When blockSize = 5, global coords should be multiplied by 3 
            this.blockScale = 3.0f;
            this.halfBlockStep = 0.5f;
            
            testTerrain = new BlockTerrainControl(CubesTestAssets.getSettings(this.app), new Vector3Int(4, 1, 4));
            testNode = new Node();
            int wallHeight = 3;
            //testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(32, 1, 64), CubesTestAssets.BLOCK_STONE);
            this.testTerrain.setBlockArea( new Vector3Int(3,0,43), new Vector3Int(10,1,10), CubesTestAssets.BLOCK_STONE);   // Zone 1 Floor
            this.testTerrain.setBlockArea( new Vector3Int(3,1,43), new Vector3Int(10,wallHeight,1), CubesTestAssets.BLOCK_WOOD);     //  South Wall
            this.testTerrain.setBlockArea( new Vector3Int(3,1,52), new Vector3Int(10,wallHeight,1), CubesTestAssets.BLOCK_WOOD);     //  North Wall
            this.testTerrain.setBlockArea( new Vector3Int(3,1,43), new Vector3Int(1,wallHeight,10), CubesTestAssets.BLOCK_WOOD);     //  West Wall
            this.testTerrain.setBlockArea( new Vector3Int(12,1,43), new Vector3Int(1,wallHeight,10), CubesTestAssets.BLOCK_WOOD);    //  East Wall
            
            this.testTerrain.removeBlockArea( new Vector3Int(12,1,46), new Vector3Int(1,wallHeight,4));                              // Door A
            
            this.testTerrain.setBlockArea( new Vector3Int(12,0,45), new Vector3Int(13,1,6), CubesTestAssets.BLOCK_STONE);   // Zone 2 Floor A
            this.testTerrain.setBlockArea( new Vector3Int(12,1,45), new Vector3Int(8,wallHeight,1), CubesTestAssets.BLOCK_WOOD);     //  South Wall
            this.testTerrain.setBlockArea( new Vector3Int(12,1,50), new Vector3Int(13,wallHeight,1), CubesTestAssets.BLOCK_WOOD);    //  North Wall
            this.testTerrain.setBlockArea(new Vector3Int(19,0,42), new Vector3Int(6,1,3), CubesTestAssets.BLOCK_STONE);     // Zone 2 Floor B
            this.testTerrain.setBlockArea( new Vector3Int(19,1,42), new Vector3Int(1,wallHeight,3), CubesTestAssets.BLOCK_WOOD);     //
            this.testTerrain.setBlockArea( new Vector3Int(24,1,42), new Vector3Int(1,wallHeight,9), CubesTestAssets.BLOCK_WOOD);     //
            
            this.testTerrain.setBlockArea( new Vector3Int(15,0,26), new Vector3Int(18,1,17), CubesTestAssets.BLOCK_STONE);  // Zone 3 Floor
            this.testTerrain.setBlockArea( new Vector3Int(15,1,42), new Vector3Int(18,wallHeight,1), CubesTestAssets.BLOCK_WOOD);    //  South Wall
            this.testTerrain.setBlockArea( new Vector3Int(15,1,26), new Vector3Int(18,wallHeight,1), CubesTestAssets.BLOCK_WOOD);    //  North Wall
            this.testTerrain.setBlockArea( new Vector3Int(15,1,26), new Vector3Int(1,wallHeight,17), CubesTestAssets.BLOCK_WOOD);    //  West Wall
            this.testTerrain.setBlockArea( new Vector3Int(32,1,26), new Vector3Int(1,wallHeight,17), CubesTestAssets.BLOCK_WOOD);    //   East Wall
            
            this.testTerrain.removeBlockArea( new Vector3Int(20,1,42), new Vector3Int(4,wallHeight,1));                              // Door B
            this.testTerrain.removeBlockArea( new Vector3Int(15,1,27), new Vector3Int(1,wallHeight,6));                              // Door C
            
            this.testTerrain.setBlockArea( new Vector3Int(10,0,26), new Vector3Int(5,1,8), CubesTestAssets.BLOCK_STONE);    // Zone 4 Floor A
            this.testTerrain.setBlockArea( new Vector3Int(10,1,26), new Vector3Int(5,wallHeight,1), CubesTestAssets.BLOCK_WOOD);     //  South Wall
            this.testTerrain.setBlockArea( new Vector3Int(3,0,18), new Vector3Int(8,1,16), CubesTestAssets.BLOCK_STONE);    // Zone 4 Floor B
            this.testTerrain.setBlockArea( new Vector3Int(3,1,18), new Vector3Int(1,wallHeight,16), CubesTestAssets.BLOCK_WOOD);    //   East Wall
            this.testTerrain.setBlockArea( new Vector3Int(10,1,18), new Vector3Int(1,wallHeight,9), CubesTestAssets.BLOCK_WOOD);    //  West Wall
            this.testTerrain.setBlockArea( new Vector3Int(3,1,33), new Vector3Int(13,wallHeight,1), CubesTestAssets.BLOCK_WOOD);     //  North Wall
            
            this.testTerrain.setBlockArea( new Vector3Int(1,0,5), new Vector3Int(26,1,14), CubesTestAssets.BLOCK_STONE);            // Zone 5
            this.testTerrain.setBlockArea( new Vector3Int(1,1,18), new Vector3Int(26,wallHeight,1), CubesTestAssets.BLOCK_WOOD);    //  South Wall
            this.testTerrain.setBlockArea( new Vector3Int(1,1,5), new Vector3Int(26,wallHeight,1), CubesTestAssets.BLOCK_WOOD);    //  North Wall
            this.testTerrain.setBlockArea( new Vector3Int(1,1,5), new Vector3Int(1,wallHeight,14), CubesTestAssets.BLOCK_WOOD);    //  West Wall
            this.testTerrain.setBlockArea( new Vector3Int(26,1,5), new Vector3Int(1,wallHeight,14), CubesTestAssets.BLOCK_WOOD);   //   East Wall
            
            this.testTerrain.removeBlockArea( new Vector3Int(4,1,18), new Vector3Int(6,wallHeight,1));                              // Door E
            
            // Populate the world with spawn points
            this.initSpawnPoints();
            
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
        
        private void initControls(){
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
            
            inputManager.addMapping("left_click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            inputManager.addListener(this, "left_click");
            inputManager.addMapping("right_click", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
            inputManager.addListener(this, "right_click");
        
            // Adding mapping and listener for change_block action
            inputManager.addMapping("change_block", new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addListener(this, "change_block");            
        }
        
        public void setVoxel(Vector3Int loc,Block blockType) {
            //testTerrain.setBlock(loc, blockType);
        }
        
        public void removeVoxel(Vector3Int loc) {
            //testTerrain.removeBlock(loc);
        }
        
        public void changeCurrentBlock() {
            currentBlock = currentBlock + 1;
            if(currentBlock > 8) {
                currentBlock = 0;
            }
            switch(currentBlock) {
                    case 0:
                    currentBlockDisplay.setText("Current Block: 0 - BLOCK_GRASS");
                    break;
                case 1:
                    currentBlockDisplay.setText("Current Block: 1 - BLOCK_WOOD");
                    break;
                case 2:
                    currentBlockDisplay.setText("Current Block: 2 - BLOCK_WOOD_FLAT");
                    break;
                case 3:
                    currentBlockDisplay.setText("Current Block: 3 - BLOCK_BRICK");
                    break;
                case 4:
                    currentBlockDisplay.setText("Current Block: 4 - BLOCK_CONNECTOR_ROD");
                    break;
                case 5:
                    currentBlockDisplay.setText("Current Block: 5 - BLOCK_GLASS");
                    break;
                case 6:
                    currentBlockDisplay.setText("Current Block: 6 - BLOCK_STONE");
                    break;
                case 7:
                    currentBlockDisplay.setText("Current Block: 7 - BLOCK_STONE_PILLAR");
                    break;
                case 8:
                    currentBlockDisplay.setText("Current Block: 8 - BLOCK_WATER");
                    break;
            }
        }
        
        @Override
        public void onAction(String action, boolean value, float lastTimePerFrame){
            
            // Implement Player Actions
            if(this.player != null) {
                if (action.equals("Left")) {
                    if (value) { player.left = true; } else { player.left = false; }
                } else if (action.equals("Right")) {
                    if (value) { player.right = true; } else { player.right = false; }
                } else if (action.equals("Up")) {
                    if (value) { player.up = true; } else { player.up = false; }
                } else if (action.equals("Down")) {
                    if (value) { player.down = true; } else { player.down = false; }
                } else if (action.equals("Jump")) {
                    player.mobControl.jump();
                }
                if (action.equals("Shoot") && !value) {
                    //Geometry p = makeProjectile(cam.getLocation(),cam.getDirection());
                    //rootNode.attachChild(p);
                    //this.bullets.add(p);
                }
            }
            
            //End of player action code
            
            //Old map-editor actions
            Vector3Int blockLocation = new Vector3Int();
            switch( this.editorState ) {
                case 0:
                    //When this.editorState == 0 -- Default state (single block add/remove)
                    if(action.equals("left_click") && value) {
                        blockLocation = getCurrentPointedBlockLocation(true);
                        if( blockLocation != null) {
                            //Add a block according to current selection
                            switch(this.currentBlock) {
                                case 0:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_GRASS);
                                    break;
                                case 1:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_WOOD);
                                    break;
                                case 2:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_WOOD_FLAT);
                                    break;
                                case 3:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_BRICK);
                                    break;
                                case 4:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_CONNECTOR_ROD);
                                    break;
                                case 5:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_GLASS);
                                    break;
                                case 6:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_STONE);
                                    break;
                                case 7:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_STONE_PILLAR);
                                    break;
                                case 8:
                                    this.setVoxel(blockLocation, CubesTestAssets.BLOCK_WATER);
                                    break;
                            }
                        }
                    } else if( action.equals("right_click") && value) {
                        blockLocation = getCurrentPointedBlockLocation(false);
                        //This conditional test ensures the bottom row is not removed.
                        if((blockLocation != null) && (blockLocation.getY() > 0)){
                            this.removeVoxel(blockLocation);
                        }
                    }
                    break;
                case 1:
                    //When this.editorState == 1 - first step of add area
                    //Set the origin point of area
                    blockLocation = getCurrentPointedBlockLocation(true);
                    this.tempAreaPos.set( blockLocation );
                    //Once the initial position is set, change the state to 2 to set x-z wifdth and length
                    break;
                case 2:
                    //When this.editorState == 2 - second step of add area
                    //Set the width and length of the area, x and z dimensions
                    blockLocation = getCurrentPointedBlockLocation(true);
                    this.tempAreaSet = blockLocation.subtract( this.tempAreaPos );
                    //After the width (x) and length (z) has been set, goto state 3
                    break;
                case 3:
                    //When this.editorState == 3 - third step of add area
                    //Set the height of the area
                    blockLocation = getCurrentPointedBlockLocation(true);
                    this.tempAreaSet.setY(blockLocation.getY()-this.tempAreaPos.getY());
                    //this.setVoxelArea( this.tempAreaPos, this.tempAreaSet );
            }
        }
    
        private Vector3Int getCurrentPointedBlockLocation(boolean getNeighborLocation){
            CollisionResults results = getRayCastingResults(this.testNode);
            if(results.size() > 0){
                Vector3f collisionContactPoint = results.getClosestCollision().getContactPoint();
                Vector3f temp = collisionContactPoint.mult(1f/5f);
                System.out.println("Map ray-cast at: (" + Math.ceil(temp.x) + ", " + Math.ceil(temp.y) + ", " + Math.ceil(temp.z) + ")" );
                return BlockNavigator.getPointedBlockLocation(this.testTerrain, collisionContactPoint, getNeighborLocation);
            }
            return null;
        }
    
        private CollisionResults getRayCastingResults(Node node){
            Vector3f origin = cam.getWorldCoordinates(new Vector2f((settings.getWidth() / 2), (settings.getHeight() / 2)), 0.0f);
            Vector3f direction = cam.getWorldCoordinates(new Vector2f((settings.getWidth() / 2), (settings.getHeight() / 2)), 0.3f);
            direction.subtractLocal(origin).normalizeLocal();
            Ray ray = new Ray(origin, direction);
            CollisionResults results = new CollisionResults();
            node.collideWith(ray, results);
            return results;
        }
        
        // Methods for appState
        @Override
        public void initialize(AppStateManager stateManager, Application app) {
            super.initialize(stateManager, app);
            this.app = app;
            
            // Prepare the physics engine
            this.bulletAppState = new BulletAppState();
            stateManager.attach(bulletAppState);
            
            // Build the map
            this.buildMap();
            this.initControls();
            this.initGUI();
            
            this.initMobs( true );
            this.initPlayer( true );
            // Set up camera Already done in global init method
            //cam.setLocation(new Vector3f(0,0,0));
            //cam.lookAtDirection(new Vector3f(0, 0, 1), Vector3f.UNIT_Y);
            //flyCam.setMoveSpeed(15);
            System.out.println("Initialized");
        }
 
        @Override
        public void stateAttached(AppStateManager stateManager) {
            super.stateAttached(stateManager);
            System.out.println("Attached");
        }
 
        @Override
        public void update(float tpf) {
            super.update(tpf);
            //Update player
            this.player.updatePlayer();
            
            //Perform raycast to tell which zone the player resides in
            Ray r = new Ray(cam.getLocation(),new Vector3f(0,-1,0));
            CollisionResults res = new CollisionResults();
            this.zoneRoot.collideWith(r, res);
            if(res.size() > 0) {
                String name = res.getClosestCollision().getGeometry().getName();
                this.currentZoneDisplay.setText("Current Zone: " + name);
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
            super.cleanup();
            System.out.println("Cleanup"); 
        }        
    }
     
}
