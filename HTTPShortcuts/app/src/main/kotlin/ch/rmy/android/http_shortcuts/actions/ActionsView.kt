package ch.rmy.android.http_shortcuts.actions

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.activities.editor.ActionListAdapter
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.DestroyerDestroyable
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotterknife.bindView

class ActionsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), DestroyerDestroyable {

    private val actionFactory = ActionFactory(context)

    var variablePlaceholderProvider: VariablePlaceholderProvider = VariablePlaceholderProvider()
        set(value) {
            field = value
            adapter.variablePlaceholderProvider = value
        }
    var isBeforeActions = false

    var actions: List<ActionDTO>
        get() = internalActions.map { it.toDTO() }
        set(value) {
            internalActions.clear()
            value.mapTo(internalActions) { actionFactory.fromDTO(it) }
            adapter.actions = internalActions
        }

    private var internalActions = mutableListOf<BaseAction>()

    private val addButton: Button by bindView(R.id.action_add_button)
    private val actionList: RecyclerView by bindView(R.id.action_list)
    private val adapter = ActionListAdapter(context)
    override val destroyer = Destroyer()

    init {
        inflate(context, R.layout.action_list, this)
        actionList.layoutManager = LinearLayoutManager(context)
        actionList.adapter = adapter

        adapter.clickListener = { action ->
            MenuDialogBuilder(context)
                .title(action.actionType.title)
                .item(R.string.action_edit_action) {
                    action.edit(context, variablePlaceholderProvider)
                        .done {
                            adapter.notifyDataSetChanged()
                        }
                }
                .item(R.string.action_remove_action) {
                    internalActions.removeAll { it.id == action.id }
                    adapter.notifyDataSetChanged()
                }
                .showIfPossible()
        }

        addButton.setOnClickListener { openAddDialog() }
        initDragOrdering()
    }

    private fun openAddDialog() {
        val actionTypes = actionFactory.availableActionTypes
            .mapIf(isBeforeActions) {
                it.filter { it.isValidBeforeAction }
            }
        MenuDialogBuilder(context)
            .title(R.string.title_add_action)
            .mapFor(actionTypes) { builder, actionType ->
                builder.item(actionType.title) {
                    val action = actionType.createAction()
                    action.edit(context, variablePlaceholderProvider)
                        .done {
                            internalActions.add(action)
                            adapter.notifyDataSetChanged()
                        }
                }
            }
            .showIfPossible()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper()
        dragOrderingHelper.attachTo(actionList)
        dragOrderingHelper.positionChangeSource
            .subscribe { (oldPosition, newPosition) ->
                val action = internalActions.removeAt(oldPosition)
                internalActions.add(newPosition, action)
                adapter.notifyItemMoved(oldPosition, newPosition)
            }
            .attachTo(destroyer)
    }

}