/*
 * The map dev tool will be split into appStates using TestAppStateLifeCycle
 * First upload, creates an empty map with start zone and first person movement. 
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
import com.cubes.network.CubesSerializer;
import com.cubes.test.CubesTestAssets;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  Tests the app state lifecycles.
 *
 *  @author    Paul Speed
 */
public class mapDevAppState extends SimpleApplication {

    public static void main(String[] args){
        mapDevAppState app = new mapDevAppState();
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
    
    public class TestState extends AbstractAppState implements ActionListener{
        // This test state is used to refactor cubes map code from mapDevToolTest
        private BlockTerrainControl testTerrain;
        private Node testNode;
        private boolean setSpawnToggle;
        private int currentBlock;
        private BulletAppState bulletAppState;
        private RigidBodyControl phyTerrain;
        
        private CharacterControl player;
        
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
                //makeProjectile(tpf);
            }
        }
        
        @Override
        public void initialize(AppStateManager stateManager, Application app) {
            // This method loads the state
            super.initialize(stateManager, app);
            System.out.println("Initialization");
            
            // Prepare the physics engine
            bulletAppState = new BulletAppState();
            stateManager.attach(bulletAppState);
                       
            // Here we init cubes engine
            CubesTestAssets.registerBlocks();
            CubesSettings blockSettings = CubesTestAssets.getSettings(app);
            blockSettings.setBlockSize(5);
            
            testTerrain = new BlockTerrainControl(CubesTestAssets.getSettings(app), new Vector3Int(2, 1, 2));
            testNode = new Node();
            testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(32, 1, 32), CubesTestAssets.BLOCK_STONE);
            testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(32, 4, 1), CubesTestAssets.BLOCK_WOOD);
            testTerrain.setBlockArea(new Vector3Int(0, 0, 0), new Vector3Int(1, 4, 32), CubesTestAssets.BLOCK_WOOD);
            testTerrain.setBlockArea(new Vector3Int(31, 0, 0), new Vector3Int(1, 4, 32), CubesTestAssets.BLOCK_WOOD);
            testTerrain.setBlockArea(new Vector3Int(0, 0, 31), new Vector3Int(32, 4, 1), CubesTestAssets.BLOCK_WOOD);
            
            testTerrain.setBlockArea(new Vector3Int(0, 0, 26), new Vector3Int(6, 4, 1), CubesTestAssets.BLOCK_GRASS);
            
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
            
            //Set up first-person view with collisions
            CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2f,4.5f,1);
            player = new CharacterControl(capsuleShape, 3f);//Step size is set in last argument
            player.setJumpSpeed(20);
            player.setFallSpeed(45);
            player.setGravity(50);
            
            player.setPhysicsLocation(new Vector3f(8f ,9f , 86f));
            bulletAppState.getPhysicsSpace().add(player);
            
            //testNode.setLocalTranslation(40, 0, 0);
            cam.setLocation(new Vector3f(8f, 9f, 86f));
            cam.lookAtDirection(new Vector3f(1, 0, 1), Vector3f.UNIT_Y);
            flyCam.setMoveSpeed(15);
            
            // Set up innput mappings
            this.initControls();
        }
 
        @Override
        public void stateAttached(AppStateManager stateManager) {
            super.stateAttached(stateManager);
            System.out.println("Attached");
        }
 
        @Override
        public void update(float tpf) {
            super.update(tpf);
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
            cam.setLocation(player.getPhysicsLocation().add(0,2.5f,0));
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
