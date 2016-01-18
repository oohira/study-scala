package controllers

import javax.inject.Inject

import controllers.UserController._
import models.Tables._
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import scala.concurrent.Future

object UserController {

  // フォームの値を格納するケースクラス
  case class UserForm(id: Option[Long], name: String, companyId: Option[Int])

  // formから送信されたデータ ⇔ ケースクラスの変換を行う
  val userForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText(maxLength = 20),
      "companyId" -> optional(number)
    )(UserForm.apply)(UserForm.unapply)
  )
}

class UserController @Inject()(val dbConfigProvider: DatabaseConfigProvider, val messagesApi: MessagesApi)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  def list = Action.async { implicit rs =>
    // IDの昇順にすべてのユーザ情報を取得
    db.run(Users.sortBy(t => t.id).result).map { users =>
      // 一覧画面を表示
      Ok(views.html.user.list(users));
    }
  }

  def edit(id: Option[Long]) = Action.async { implicit rs =>
    val form = if (id.isDefined) {
      // リクエストパラメータにIDが存在する場合
      db.run(Users.filter(t => t.id === id.get.bind).result.head).map { user =>
        userForm.fill(UserForm(Some(user.id), user.name, user.companyId))
      }
    } else {
      // リクエストパラメータにIDが存在しない場合
      Future {
        userForm
      }
    }
    form.flatMap { form =>
      db.run(Companies.sortBy(_.id).result).map { companies =>
        Ok(views.html.user.edit(form, companies))
      }
    }
  }

  def create = TODO

  def update = TODO

  def remove(id: Long) = TODO
}
