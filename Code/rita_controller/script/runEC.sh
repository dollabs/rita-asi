function runEC() {
    echo $1 $2
    cd ../../Code/doll-components/
    java -jar target/experiment-control.jar -x ../rita_controller/public/data/ECdata/$1 -l ../rita_controller/public/upload/$2
    # java -jar target/experiment-control.jar -x ../rita_controller/public/data/ECdata/exp-0002 -l ../rita_controller/public/data/ECdata/study-1_2020.08-rmq/HSRData_TrialMessages_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-188_Team-na_Member-74_Vers-3.log

}

runEC $1 $2