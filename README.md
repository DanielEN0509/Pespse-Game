
<img width="1911" height="1072" alt="צילום מסך 2026-05-14 081027" src="https://github.com/user-attachments/assets/beb69743-6075-4461-ac4c-238742d18bf6" />
PEPSE - Procedural Environment Professional Simulation Engine

An infinite 2D procedural world-generation engine implemented in Java. This project simulates a dynamic environment including terrain, organic tree growth, and a physics-based avatar system.

🚀 Architectural Highlights
🌳 Modular Flora System (Factory & Composite)
Procedural Generation: The Flora class acts as a factory, using probabilistic logic to orchestrate tree placement across the terrain.  
Decoupling via Callbacks: To maintain strict decoupling, Flora retrieves ground heights from the Terrain using a Function callback, ensuring trees are planted precisely without direct class dependency.  
Complex Object Composition: Each tree is a composite of specialized classes: Trunk (static support), Leaf (animated via Transition), and Fruit (interactive resource).  

👤 Advanced Avatar Mechanics
Finite State Machine (FSM): Implemented a lightweight FSM using an internal State Enum to manage transitions and animations, ensuring high readability and maintainability.  Dynamic Energy Management:
Real-time energy consumption for running and jumping.  
Recovery logic specifically during IDLE states on the ground.  
UI Decoupling: The energy display uses a Supplier callback to fetch values, ensuring the UI stays updated without rigid dependencies.  
Physics Optimization: Solved "jittering" issues by implementing a strict velocity-cutoff when energy is zero, ensuring a smooth user experience even during exhaustion.

🍃 Realism & AnimationsWind Simulation: 
Leaves utilize Transition components for rotation and scaling, triggered by ScheduledTask with randomized delays to prevent unnatural synchronicity.  
Interactive Environment: Fruits use a Consumer callback to interact with the Avatar’s energy pool and feature a 30-second respawn cycle.  

💻 Technical Skills Demonstrated
Java & OOP: Inheritance, internal Enums, and advanced decoupling techniques.
Design Patterns & Principles: Factory Pattern, Composite Pattern, and Functional Interfaces (Supplier, Consumer, Function).
<img width="1911" height="1072" alt="צילום מסך 2026-05-14 081027" src="https://github.com/user-attachments/assets/6a29953d-eea1-4f64-b17c-d42609767e36" />
Game Physics: Collision handling, velocity management, and deltaTime-based calculations.
