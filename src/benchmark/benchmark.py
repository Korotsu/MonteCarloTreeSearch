import subprocess
import sys
import os
import matplotlib.pyplot as plt
from typing import List

class BenchData:
    def __init__(self, domain: str, planner: str, args: str, counterMin: int, counterMax: int):
        """Initialize the BenchData class

        Args:
            domain (str): The domain file for evaluation.
            planner (str): The planner to evaluate.
            args (str): Optional additional arguments for the planner.
            counterMin (int): The optional minimum starting index for the problem files (inclusive).
            counterMax (int): The optional maximum ending index for the problem files (inclusive).
        """
        self.domain = domain
        self.planner = planner
        self.args = args
        self.counterMin = counterMin
        self.counterMax = counterMax
        
    def print(self):
        """Print all the class data in the console.
        """
        print("Domain : " + self.domain)
        print("Planner : " + self.planner)
        print("Args : " + self.args)
        maxText = "max" if(self.counterMax == sys.maxsize) else self.counterMax
        print("From test " + str(self.counterMin) + " to test " + str(maxText))
    
class BenchResult:
    def __init__(self, domain: str, pbIndex: int, planner: str, planLength: int, timeSpent: float):
        """Initialize the BenchResult class

        Args:
            domain (str): The domain file of evaluation.
            pbIndex (int): The problem file index.
            planner (str): The planner evaluated.
            planLength (int): The length of the found plan.
            timeSpent (float): The time spent to find the plan.
        """
        self.domain = domain
        self.pbIndex = pbIndex
        self.planner = planner
        self.planLength = planLength
        self.timeSpent = timeSpent
    
    def print(self):
        """Print all the class data in the console.
        """
        print("Domain : " + self.domain)
        print("Planner : " + self.planner)
        print("Problem index : " + str(self.pbIndex))
        print("Plan length : " + str(self.planLength))
        
        #Convert time spent from millisecond to second.
        time = self.timeSpent/1000.0
        print("Time spent : " + str(time) + " seconds")
        
    def getEvaluatedVar(self, varName: str):
        """Get the evaluated variable.

        Args:
            varName (str): The name of the evaluated variable.

        Returns:
            (int | float | None): The data of the evaluated variable.
        """
        if varName == "planLength":
            return self.planLength
        elif varName == "timeSpent":
            #Return time spent as second
            return self.timeSpent/1000
        else:
            return None

# GraphParam class is used to gather information for a plot.
class GraphParam:
    def __init__(self, evaluatedDomain:str, evaluatedVar:str, xAxisPlanner: str):
        """Initialize the GraphParam class

        Args:
            evaluatedDomain (str): The domain to plot. 
            evaluatedVar (str): The variable to plot.
            xAxisPlanner (str): The planner to use as the referential for the x-axis.
        """
        self.evaluatedDomain = evaluatedDomain
        self.evaluatedVar = evaluatedVar
        self.xAxisPlanner = xAxisPlanner

def benchmark(data: BenchData):
    """The function used to automatize the benchmarks of planners.

    Args:
        data (BenchData): The data needed for the benchmark.

    Returns:
        List(BenchResult): The list with all the benchmarks results.
    """
    #Initialize all required data.
    benchmarkFolder = data.domain.split("domain")[0]
    
    #Count bench files in the folder -1 to remove the domain file.
    benchCount = len(os.listdir(rootFolder + benchmarkFolder)) - 1
    powerOfTenCount = 0
    
    #Calculate the power of ten to format the counter of the benchmarkFile.
    while benchCount >= 1:
        benchCount /= 10
        powerOfTenCount += 1
        
    benchmarkFile = benchmarkFolder + "p{0}.pddl"
    benchmarkFile = benchmarkFile.format("{:0" + str(powerOfTenCount) + "d}")
    counter = data.counterMin
    resultList = []
    print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    print("Start benchmark of :")
    data.print()
    print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    #Iterate on all file in the evaluated domain folder.
    while counter <= data.counterMax and os.path.exists(rootFolder + benchmarkFile.format(counter)):
        try:
            #Execute the benchmark. 
            ans = subprocess.check_output(cmd.format(planner=data.planner, domain=data.domain,
                                                    benchmarkFile=benchmarkFile.format(counter), 
                                                    args=data.args), text=True, cwd="../../")
            result = BenchResult(domain=data.domain, pbIndex=counter, planner=data.planner, planLength=0, timeSpent=0)
            print(ans)
            
            #Get the result of the benchmark 
            if len(ans.split("STATS:")) <= 1:
                counter += 1
                continue
            stats = ans.split("STATS:")[1]
            result.timeSpent = float(stats.split("TIME=")[1].split(";")[0])
            result.planLength = int(stats.split("PLAN=")[1].split(";")[0])
            resultList.append(result)
            if shouldSaveResult:
                #Save the results.
                save([result])
        except subprocess.CalledProcessError as e: 
            print(f"Command failed with return code {e.returncode}")
        counter += 1
        
    return resultList

def save(results: List[BenchResult]):
    """The function to save the results of benchmarks.

    Args:
        results (List[BenchResult]): All the benchmarks results.
    """
    cwd = os.getcwd()
    if os.path.isdir(cwd + "\\benchmarks") == False:
        os.mkdir(cwd + "\\benchmarks")
    
    filePath = "{domainName}\\{benchName}\\benchmark{benchNumber:04d}.benchDATA"
    #For each results save it as a new benchData file.
    for result in results:
        domainName = result.planner.split('.')[-1]
        benchName = result.domain.split('/')[-3]
        counter = 1
        #Find the last created file index.
        while os.path.exists(cwd + "\\benchmarks\\" + filePath.format(domainName=domainName,benchName=benchName,benchNumber=counter)):
            counter += 1
        os.makedirs(cwd + "\\benchmarks\\" + domainName + '\\' + benchName, exist_ok=True)
        file = cwd + "\\benchmarks\\" + filePath.format(domainName=domainName,benchName=benchName,benchNumber=counter)    
        with open(file, 'w') as f:
            data = "domain={domain};pbIndex={pbIndex};planner={planner};planLength={planLength};timeSpent={timeSpent};"
            f.write(data.format(domain=result.domain, pbIndex=result.pbIndex, planner=result.planner,
                                planLength=result.planLength, timeSpent=result.timeSpent))
            f.close()
            
def load(benchsToLoad: List[str]):
    """The function used to load a list of benchData files.

    Args:
        benchsToLoad (List[str]): The list of benchData files to load.
    """
    for benchFile in benchsToLoad:
        if os.path.exists(rootFolder + benchFile) == False:
            continue
        #Open the file and read the data from it
        with open(rootFolder + benchFile, 'r') as f:
            data: str = f.read()
            domain = data.split("domain=")[1].split(";")[0]
            pbIndex = int(data.split("pbIndex=")[1].split(";")[0])
            planner = data.split("planner=")[1].split(";")[0]
            planLength = int(data.split("planLength=")[1].split(";")[0])
            timeSpent = float(data.split("timeSpent=")[1].split(";")[0])
            bench = BenchResult(domain=domain, pbIndex=pbIndex, planner=planner, planLength=planLength, timeSpent=timeSpent)
            print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            print("Benchmark loaded :")
            bench.print()
            print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            graphDataList.append(bench)

def graph(param: GraphParam):
    """The function used to plot the benchmarks results.

    Args:
        param (GraphParam): Parameters required for plots.
    """
    plannerSet = set()
    #Gather the list of planners to find if the referential one for the x-axis exist.
    for data in graphDataList:
        plannerSet.add(data.planner.split('.')[-1])
    
    #If no planner are found then leave the function.
    if len(plannerSet) < 1:
        return
    
    #If the x-axis referential planner is not found then take the first one as the referential.
    if plannerSet.__contains__(param.xAxisPlanner) == False:
        param.xAxisPlanner = plannerSet[0]
        
    xpoints = {}
    dataDict = {}
    #Loop over all available data.
    for data in graphDataList:
        #Check if the data correspond to the evaluated domain and also if the evaluated variable is valid.
        if data.domain.split('/')[-3] == param.evaluatedDomain and data.getEvaluatedVar(param.evaluatedVar) != None:
            #Get the name of the planner exemple : MCTS.
            dataPlanner = data.planner.split('.')[-1]
            #Initialize the sub-dictionnary if it didn't exist.
            if dataDict.get(dataPlanner) == None:
                dataDict[dataPlanner] = {}
            dataDict[dataPlanner][data.pbIndex] = data.getEvaluatedVar(param.evaluatedVar)
            #Gather information to sort the x-axis.
            if dataPlanner == param.xAxisPlanner:
                xpoints[data.pbIndex] = data.getEvaluatedVar(param.evaluatedVar)
    
    #Sort the x-axis data relative to the evaluated variable.
    sortedX = dict(sorted(xpoints.items(), key= lambda item: item[1]))
    for planner in dataDict.keys():
        tempYvalues = []
        #Sort the Y values for each planner relative to the sorted x-axis values.
        for x in sortedX.keys():
            tempYvalues.append(float(dataDict[planner].get(x, 0)))
        plt.plot(sortedX.keys(), tempYvalues, marker = 'o', label=planner)
    
    #Show the result.
    plt.title("Benchmarks results")
    plt.xlabel("Problem index")
    plt.legend(fancybox=True, framealpha=1, shadow=True, borderpad=1)
    plt.ylabel(param.evaluatedVar if param.evaluatedVar != "timeSpent" else param.evaluatedVar + " (seconds)")
    plt.grid(linestyle = '--', linewidth = 0.5)
    plt.show()
    
        
def initialize(shouldSaveResult:bool):
    """Initialize the system with the command line arguments.

    Args:
        shouldSaveResult (bool): The flag to tell the system to save or not the benchmarks results.

    Returns:
        bool: The flag to tell the system to save or not the benchmarks results.
    """
    for i in range(0, len(args)):
        #Adds a new GraphParam to plot with the data supplied as arguments in the command line.            
        if((args[i].startswith("-g") or args[i].startswith("--graph")) and (len(args[i].split("=", 1)) > 1)):
            param = args[i].split("=", 1)[1]
            domain = param.split("domain=")[1].split(";")[0]
            variable = param.split("variable=")[1].split(";")[0]
            planner = param.split("planner=")[1].split(";")[0]
            graphParamList.append(GraphParam(domain,variable,planner))
        
        #Tell the system to save the benchmarks results.
        if(args[i] == "-s" or args[i] == "--save"):
            shouldSaveResult = True

        #Adds a new BenchData to benchmark with the data supplied as arguments in the command line. 
        if ((args[i].startswith("-b") or args[i].startswith("--bench")) and (len(args[i].split("=", 1)) > 1)):
            bench = args[i].split("=", 1)[1]
            #If there is no domain or planner, continue looping without taking this command into account.
            if(len(bench.split("domain=")) <= 1 or len(bench.split("planner=")) <= 1):
                continue
            
            domain = bench.split("domain=")[1].split(";")[0]
            #Check if the domain exist.
            if(os.path.exists(rootFolder + domain) == False):
                continue

            planner = bench.split("planner=")[1].split(";")[0]
            benchArgs = bench.split("args=")[1].split(";")[0] if (len(bench.split("args=")) > 1) else ""
            counterMin = int(bench.split("min=")[1].split(";")[0]) if (len(bench.split("min=")) > 1) else 1
            counterMin = counterMin if counterMin >= 1 else 1
            counterMax = int(bench.split("max=")[1].split(";")[0]) if (len(bench.split("max=")) > 1) else sys.maxsize
            counterMax = counterMax if counterMax > counterMin else counterMin
            benchList.append(BenchData(domain, planner, benchArgs, counterMin, counterMax))
        
        #Adds a new file/folder to load. 
        if((args[i].startswith("-l") or args[i].startswith("--load")) and (len(args[i].split("=", 1)) > 1)):
            loadArg =  args[i].split("=", 1)[1]
            if os.path.isfile(rootFolder + loadArg):
                loadList.append(rootFolder + loadArg)
            elif os.path.isdir(rootFolder + loadArg):
                for file in os.listdir(rootFolder + loadArg):
                    if os.path.isfile(rootFolder + loadArg + '\\' + file):
                        loadList.append(loadArg + '\\' + file)

    return shouldSaveResult

def main():
    """The main flow of the program.
    """
    for bench in benchList:
        #Execute a benchmark for each benchmark data available.
        results = benchmark(bench)
        #Add the results to the graphDataList.
        graphDataList.extend(results)
    
    #Load the requested benchmark data.
    load(loadList)
    
    for graphParam in graphParamList:
        #Plot the graph data.
        graph(graphParam)
    
if __name__ == '__main__':
    cmd = "java -cp classes;lib/pddl4j-4.0.0.jar {planner} {domain} {benchmarkFile} {args}"
    args = sys.argv
    rootFolder = os.getcwd().split("src")[0]
    loadList : List[str] = []
    benchList : List[BenchData] = []
    graphDataList : List[BenchResult] = []
    graphParamList: List[GraphParam] = []
    shouldSaveResult = initialize(False)
    main()