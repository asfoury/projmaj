package com.sdp13epfl2021.projmag.model

import com.sdp13epfl2021.projmag.database.Utils

/**
 *  An Immutable project Filter
 *  It's constructor takes filtering parameters and its `invoke` function operator
 *  takes a project and return `true`/`false`, whether it matches or not the constructor
 *  parameters
 *
 *  **If applied is used you should define a check function with `setApplicationCheck` otherwise
 *  no filtering will be applied**
 *
 * @property bachelor if it is true will require the project degree be bachelor, otherwise it has
 *                    no effect on filtering.
 * @property master if it is true will require the project degree to be master, otherwise it has
 *                  no effect on filtering.
 * @property applied if it is true will require the project to be one the user applied to
 *                   (i.e. `isAppliedProject` is true), otherwise it has no effect on filtering.
 */
class ProjectFilter(
    val bachelor: Boolean = false,
    val master: Boolean = false,
    val applied: Boolean = false,
    val favorites: Boolean = true
) {

    companion object {
        /**
         * Create a ProjectFilter from a Map of field name to the value
         *
         * @param data the map field name -> value
         * @return a ProjectFilter built from the value in the `data` map
         */
        operator fun invoke(data: Map<String, Any>): ProjectFilter =
            ProjectFilter(
                bachelor = data["bachelor"] as? Boolean ?: false,
                master = data["master"] as? Boolean ?: false,
                applied = data["applied"] as? Boolean ?: false,
                favorites = data["favorites"] as? Boolean ?: false
            )

    }

    /**
     * A function used to see if the user has applied to the project
     */
    private var isAppliedProject: ((ImmutableProject) -> Boolean)? = null
    private var isFavouriteProject : ((ImmutableProject) -> Boolean)? = null

    /**
     * Tells if the given project match the constraints
     *
     * @param project the project to check constraint
     *
     * @return `true` if it matches, `false` otherwise
     */
    operator fun invoke(project: ImmutableProject): Boolean {
        var matches = true
        if (bachelor) {
            matches = matches && project.bachelorProject
        }
        if (master) {
            matches = matches && project.masterProject
        }
        if (applied) {
            matches = matches && (isAppliedProject?.let { it(project) } ?: true)
        }
        if(favorites){
            matches = matches && (isFavouriteProject?.let { it(project) } ?: true)
        }
        return matches
    }

    /**
     * Set the function that will check if the project is one the those the user applied to.
     *
     * @param appCheck Function that take a project and indicates if the user applied to it
     *
     * @return this filter
     */
    fun setApplicationCheck(appCheck: (ImmutableProject) -> Boolean): ProjectFilter {
        isAppliedProject = appCheck
        return this
    }

    /**
     * Function that will check if the project is contained in the favorites
     *
     * @param favCheck : Function that will take a project and indicate if it's in the favorite list
     * @return
     */
    fun setFavouriteCheck(favCheck: (ImmutableProject) -> Boolean): ProjectFilter {
        isFavouriteProject = favCheck
        return this
    }


}
