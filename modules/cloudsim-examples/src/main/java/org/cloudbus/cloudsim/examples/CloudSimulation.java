package org.cloudbus.cloudsim.examples;// Filename: CloudSimulation.java

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.*;

public class CloudSimulation {
    public static void main(String[] args) {
        runScenario("Scenario 1", false);
//        runScenario("Scenario 2", true);
    }

    private static void runScenario(String scenarioName, boolean equalTraffic) {
        // Step 1: Initialize CloudSim
        initializeCloudSim();

        // Step 2: Create Datacenter
        Datacenter datacenter = createDatacenter("Datacenter_0");

        // Step 3: Create Broker
        DatacenterBroker broker = createBroker();

        // Step 4: Create Servers
        List<Vm> serverList = createServers(broker.getId());

        // Step 5: Create Load Balancer
        ReadOptimizedLoadBalancer loadBalancer = createLoadBalancer(serverList, broker);

        // Create Clients
        List<Cloudlet> clientList = createClients(broker.getId(), equalTraffic);
        startSimulation(broker, serverList, clientList, loadBalancer);
        measureLatency(broker, scenarioName);
        measureThroughput(broker, scenarioName);
        measureResourceUtilization(datacenter, scenarioName);
    }

    private static void initializeCloudSim() {
        try {
            // Number of cloud users
            int numUsers = 1;
            // Calendar object to store the current simulation time
            Calendar calendar = Calendar.getInstance();
            // Trace flag. Setting to false means that trace events will not be logged.
            boolean traceFlag = false;

            // Initialize the CloudSim library
            CloudSim.init(numUsers, calendar, traceFlag);

            // Display a log message indicating that CloudSim has been initialized
            Log.printLine("CloudSim initialized.");
        } catch (Exception e) {
            // Handle any exceptions that occur during initialization
            e.printStackTrace();
            Log.printLine("Failed to initialize CloudSim.");
        }
    }

    private static Datacenter createDatacenter(String name) {
        try {
            // 1. Create a list to store one or more machines
            List<Host> hostList = new ArrayList<Host>();

            // Machine configuration : 2 hosts, each with 1 core of 1000 MIPS capacity
            int mips = 1000;
            int ram = 2048; // host memory (MB)
            long storage = 1000000; // host storage
            int bw = 10000; // bandwidth

            // 2. Create PEs (Processing Elements, i.e., CPUs/Cores)
            List<Pe> peList1 = new ArrayList<Pe>();
            peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // First host: 1 core

            List<Pe> peList2 = new ArrayList<Pe>();
            peList2.add(new Pe(0, new PeProvisionerSimple(mips))); // Second host: 1 core

            // 3. Create Hosts with its id and list of PEs
            Host host1 = new Host(0, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
                    storage, peList1, new VmSchedulerTimeShared(peList1)); // First host

            Host host2 = new Host(1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
                    storage, peList2, new VmSchedulerTimeShared(peList2)); // Second host

            // Add hosts to the host list
            hostList.add(host1);
            hostList.add(host2);

            // 4. Create a DatacenterCharacteristics object
            String arch = "x86"; // system architecture
            String os = "Linux"; // operating system
            String vmm = "Xen";
            double time_zone = 10.0; // time zone this resource located
            double cost = 3.0; // the cost of using processing in this resource
            double costPerMem = 0.05; // the cost of using memory in this resource
            double costPerStorage = 0.001; // the cost of using storage in this resource
            double costPerBw = 0.0; // the cost of using bandwidth in this resource
            LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

            DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                    arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

            // 5. Create a Datacenter object

            return new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Error creating datacenter.");
            return null;
        }
    }


    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Error creating broker.");
        }
        return broker;  // Return the DatacenterBroker object
    }


    private static List<Vm> createServers(int brokerId) {
        List<Vm> vmlist = new ArrayList<Vm>();

        // VM description
        int vmid = 0;
        int mips = 250;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        long bw = 1000;
        int pesNumber = 1; // number of cpus
        String vmm = "Xen"; // VMM name

        // Create two VMs
        Vm vm1 = new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
        Vm vm2 = new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

        // Add the VMs to the vmList
        vmlist.add(vm1);
        vmlist.add(vm2);

        return vmlist;
    }


    private static ReadOptimizedLoadBalancer createLoadBalancer(List<Vm> serverList, DatacenterBroker broker) {
        ReadOptimizedLoadBalancer loadBalancer = new ReadOptimizedLoadBalancer(serverList, broker);
        return loadBalancer;
    }

    private static ReadCloudlet createReadCloudlet(int id, int brokerId) {
        int pesNumber = 1;
        long length = 10000;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        ReadCloudlet cloudlet = new ReadCloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet.setUserId(brokerId);
        return cloudlet;
    }

    private static WriteCloudlet createWriteCloudlet(int id, int brokerId) {
        int pesNumber = 1;
        long length = 250000;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        WriteCloudlet cloudlet = new WriteCloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet.setUserId(brokerId);
        return cloudlet;
    }


    private static List<Cloudlet> createClients(int brokerId, boolean equalTraffic) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        int id = 0;
        int totalRequests = 100;

        for (int i = 0; i < totalRequests; i++) {
            long length = (long) (Math.random() * 100000);  // Varying request sizes
            Cloudlet cloudlet;
            if (equalTraffic) {
                // Scenario 2: Equal amount of read and write traffic
                cloudlet = (i % 2 == 0) ? createReadCloudlet(id++, brokerId) : createWriteCloudlet(id++, brokerId);
            } else {
                // Scenario 1: Heavy read traffic and some write traffic
                cloudlet = (i < (totalRequests * 0.8)) ? createReadCloudlet(id++, brokerId) : createWriteCloudlet(id++, brokerId);
            }
            cloudlet.setCloudletLength(length);  // Varying request sizes
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }


    private static void startSimulation(DatacenterBroker broker, List<Vm> serverList, List<Cloudlet> clientList, ReadOptimizedLoadBalancer loadBalancer) {
        // Submit the VM list to the broker
        broker.submitVmList(serverList);

        // Replace direct submission of cloudlets to the broker with submission through the loadBalancer
        loadBalancer.submitCloudletList(clientList);

        // Start the simulation
        CloudSim.startSimulation();

        // Stop the simulation
        CloudSim.stopSimulation();
    }


    private static void measureLatency(DatacenterBroker broker, String scenario) {
        List<Cloudlet> resultList = broker.getCloudletReceivedList();
        double totalReadTime = 0;
        double totalWriteTime = 0;
        int readCount = 0;
        int writeCount = 0;
        for (Cloudlet cloudlet : resultList) {
            System.out.println("Start time: " + cloudlet.getFinishTime());
            double latency = cloudlet.getFinishTime() - cloudlet.getExecStartTime();

            if (cloudlet instanceof ReadCloudlet) {
//                System.out.println("Latency Read: " + latency);
                totalReadTime += latency;
                readCount++;
            } else if (cloudlet instanceof WriteCloudlet) {
                System.out.println("Latency Write: " + latency);
                totalWriteTime += latency;
                writeCount++;
            }
        }
        if (readCount > 0) {
            double averageReadTime = totalReadTime / readCount;
            System.out.println("Average read time for " + scenario + ": " + averageReadTime + " units.");
        }
        if (writeCount > 0) {
            double averageWriteTime = totalWriteTime / writeCount;
            System.out.println("Average write time for " + scenario + ": " + averageWriteTime + " units.");
        }
    }


    private static void measureThroughput(DatacenterBroker broker, String scenario) {
        List<Cloudlet> resultList = broker.getCloudletReceivedList();
        double simulationTime = CloudSim.clock();  // Get the total simulation time
        int totalCompletedCloudlets = resultList.size();  // Total completed cloudlets
        double throughput = totalCompletedCloudlets / simulationTime;  // Throughput calculation
        System.out.println("Throughput for " + scenario + ": " + throughput + " cloudlets/unit time.");
    }


    private static void measureResourceUtilization(Datacenter datacenter, String scenario) {
        List<Host> hostList = datacenter.getHostList();
        double totalMipsUtilization = 0;
        double totalRamUtilization = 0;
        double totalBwUtilization = 0;

        for (Host host : hostList) {
            for (Vm vm : host.getVmList()) {
                totalMipsUtilization += vm.getTotalUtilizationOfCpu(CloudSim.clock());
                totalRamUtilization += vm.getCurrentAllocatedRam();  // This gives the current allocated RAM
                totalBwUtilization += vm.getCurrentAllocatedBw();  // This gives the current allocated bandwidth
            }
        }

        int totalHosts = hostList.size();
        double avgMipsUtilization = totalMipsUtilization / totalHosts;
        double avgRamUtilization = totalRamUtilization / totalHosts;
        double avgBwUtilization = totalBwUtilization / totalHosts;

        System.out.println("Average CPU (MIPS) utilization for " + scenario + ": " + avgMipsUtilization);
        System.out.println("Average RAM utilization for " + scenario + ": " + avgRamUtilization);
        System.out.println("Average Bandwidth utilization for " + scenario + ": " + avgBwUtilization);
    }
}

// Dummy LoadBalancer class, replace with actual implementation
class LoadBalancer {
    private final List<Vm> vmList;
    private final DatacenterBroker broker;
    private int nextVmIndex = 0;

    public LoadBalancer(List<Vm> vmList, DatacenterBroker broker) {
        this.vmList = vmList;
        this.broker = broker;
    }

    public void submitCloudlet(Cloudlet cloudlet) {
        // Create a temporary list to hold the cloudlet
        List<Cloudlet> tempCloudletList = new ArrayList<>();
        tempCloudletList.add(cloudlet);

        // Submit the temporary list to the broker
        broker.submitCloudletList(tempCloudletList);

        // Get the next VM in the list
        Vm vm = vmList.get(nextVmIndex);

        // Bind the cloudlet to this VM
        broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());

        // Update the index for the next VM
        nextVmIndex = (nextVmIndex + 1) % vmList.size();
    }

    public void submitCloudletList(List<Cloudlet> cloudletList) {
        // Submit the cloudlet list to the broker
        broker.submitCloudletList(cloudletList);

        for (Cloudlet cloudlet : cloudletList) {
            // Get the next VM in the list
            Vm vm = vmList.get(nextVmIndex);

            // Bind the cloudlet to this VM
            broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());

            // Update the index for the next VM
            nextVmIndex = (nextVmIndex + 1) % vmList.size();
        }
    }
}



class ReadCloudlet extends Cloudlet {
    // Constructor that just calls the super constructor
    public ReadCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel cloudletUtilizationModelCpu, UtilizationModel cloudletUtilizationModelRam, UtilizationModel cloudletUtilizationModelBw) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, cloudletUtilizationModelCpu, cloudletUtilizationModelRam, cloudletUtilizationModelBw);
    }
}

class WriteCloudlet extends Cloudlet {
    // Constructor that just calls the super constructor
    public WriteCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel cloudletUtilizationModelCpu, UtilizationModel cloudletUtilizationModelRam, UtilizationModel cloudletUtilizationModelBw) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, cloudletUtilizationModelCpu, cloudletUtilizationModelRam, cloudletUtilizationModelBw);
    }
}

class ReadOptimizedLoadBalancer extends LoadBalancer {
    private final List<Vm> vmList;
    private final DatacenterBroker broker;
    private int nextVmIndex = 0;  // Index to keep track of the next VM to use

    // Cache to store frequently read cloudlets
    private Map<Integer, Integer> readCache = new HashMap<>();

    public ReadOptimizedLoadBalancer(List<Vm> vmList, DatacenterBroker broker) {
        super(vmList, broker);
        this.vmList = vmList;
        this.broker = broker;
    }

    @Override
    public void submitCloudlet(Cloudlet cloudlet) {
        Vm selectedVm = null;
        if (cloudlet instanceof ReadCloudlet) {
            // Check if cloudlet is in cache
            if (readCache.containsKey(cloudlet.getCloudletId())) {
                bindAndSubmit(cloudlet, vmList.get(readCache.get(cloudlet.getCloudletId())));
                return;
            }

            // For read requests, find the VM with the least number of write requests
            int minWriteCount = Integer.MAX_VALUE;
            for (Vm vm : vmList) {
                int writeCount = countWriteCloudlets(vm);
                if (writeCount < minWriteCount) {
                    minWriteCount = writeCount;
                    selectedVm = vm;
                }
            }
            // Update cache with the new binding
            readCache.put(cloudlet.getCloudletId(), selectedVm.getId());
        } else {
            // For write requests, use a simple round-robin algorithm
            selectedVm = getNextVm();
        }
        bindAndSubmit(cloudlet, selectedVm);
    }

    private int countWriteCloudlets(Vm vm) {
        int writeCount = 0;
        for (ResCloudlet resCloudlet : vm.getCloudletScheduler().getCloudletExecList()) {
            Cloudlet cloudlet = resCloudlet.getCloudlet();  // Get the underlying Cloudlet object
            if (cloudlet instanceof WriteCloudlet) {
                writeCount++;
            }
        }
        return writeCount;
    }

    private Vm getNextVm() {
        // Get the next VM in the list using the current index
        Vm nextVm = vmList.get(nextVmIndex);

        // Increment the index for the next call, wrapping around to 0 if at the end of the list
        nextVmIndex = (nextVmIndex + 1) % vmList.size();

        return nextVm;  // Return the selected VM
    }

    private void bindAndSubmit(Cloudlet cloudlet, Vm vm) {
        List<Cloudlet> tempCloudletList = new ArrayList<>();
        tempCloudletList.add(cloudlet);
        broker.submitCloudletList(tempCloudletList);
        broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());
    }
}

//| Parameter                             | Value         | Notes                                                                   |
//        |---------------------------------------|---------------|-------------------------------------------------------------------------|
//        | **Processing Element (PE) Configuration** |               |                                                                         |
//        | MIPS per core                         | 1000          | Million Instructions Per Second                                          |
//        | Number of cores per host              | 1             |                                                                         |
//        | **Host Configuration**                    |               |                                                                         |
//        | RAM                                   | 2048 MB       |                                                                         |
//        | Storage                               | 1,000,000 MB  |                                                                         |
//        | Bandwidth                             | 10,000 MB/s   |                                                                         |
//        | **Virtual Machine (VM) Configuration**    |               |                                                                         |
//        | MIPS per VM                           | 250           | Assuming each VM utilizes a single core                                  |
//        | Number of VMs                         | 2             |                                                                         |
//        | RAM per VM                            | 512 MB        |                                                                         |
//        | Storage per VM                        | 10,000 MB     |                                                                         |
//        | Bandwidth per VM                      | 1,000 MB/s    |                                                                         |
//        | **Cloudlet Configuration**                |               |                                                                         |
//        | Read Cloudlets Length                 | 10,000        |                                                                         |
//        | Write Cloudlets Length                | 250,000       |                                                                         |
//        | File Size                             | 300           |                                                                         |
//        | Output Size                           | 300           |                                                                         |
//        | **Load Balancer Configuration**          |               |                                                                         |
//        | Read-Optimized Load Balancer          | -             | Utilizing a simple cache mechanism                                       |
//        | Least Write Requests Strategy         | -             | Distributing read requests to servers with the least number of write requests |
//        | **Simulation Settings**                   |               |                                                                         |
//        | Total Requests                        | 100, 1,000, 10,000| Different scenarios                                                  |
//        | Equal Traffic Scenario                | Disabled      |
//
//


