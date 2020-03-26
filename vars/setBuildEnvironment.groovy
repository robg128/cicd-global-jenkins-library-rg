/**
 * Sets environment variables for the build.
 *
 * @param config the pipeline config
  * @param environment for pipeline config
 */
 
def call(Map config = [:], environment) {

    def defaults = [
        // DEV -> Dev
        "DEV": [
            TF_VAR_bucket: "bck-mstr-us-east1-tfadmin-qa-state",
            TF_VAR_credentials: "mstr-tfadmin-dev-32f6-3fdb8acceb66.json",        
            planOutFile: "tf-search-dev.tfplan",
            varFile: "dev.tfvars",
        ],
        // QA -> QA
        "QA": [
            TF_VAR_bucket: "bck-mstr-us-east1-tfadmin-qa-state",
            TF_VAR_credentials: "mstr-tfadmin-dev-32f6-3fdb8acceb66.json",        
            planOutFile: "tf-search-dev.tfplan",
            varFile: "dev.tfvars",
        ],
        // PRD -> Production
        "PRD": [
            TF_VAR_bucket: "bck-mstr-us-east1-tfadmin-qa-state",
            TF_VAR_credentials: "mstr-tfadmin-dev-32f6-3fdb8acceb66.json",        
            planOutFile: "tf-search-dev.tfplan",
            varFile: "dev.tfvars",
        ]
    ]

    // checks if ENVIRONMENT is valid
    if (!defaults[environment]) {
        error "Invalid 'ENVIRONMENT' (value='${environment}'). Known locations are: ${defaults.keySet()}. Please contact your Jenkins administrator to define the 'ENVIRONMENT'  var."
    }

    // set the location defaults on the pipeline if not already defined
    defaults[environment].each { key, defaultValue ->
        if (!config.containsKey(key)) config[key] = defaultValue
    }
}